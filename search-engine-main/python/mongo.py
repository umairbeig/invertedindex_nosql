import argparse
import contextlib
import sys

import joblib
from pymongo import MongoClient
from tqdm import tqdm


@contextlib.contextmanager
def tqdm_joblib(tqdm_object):
    """Context manager to patch joblib to report into tqdm
    progress bar given as argument"""

    class TqdmBatchCompletionCallback(joblib.parallel.BatchCompletionCallBack):
        def __call__(self, *args, **kwargs):
            tqdm_object.update(n=self.batch_size)
            return super().__call__(*args, **kwargs)

    old_batch_callback = joblib.parallel.BatchCompletionCallBack
    joblib.parallel.BatchCompletionCallBack = TqdmBatchCompletionCallback
    try:
        yield tqdm_object
    finally:
        joblib.parallel.BatchCompletionCallBack = old_batch_callback
        tqdm_object.close()


def convert_line(line: str) -> dict[str, list[dict[str, str | int]]]:
    assert len(line.strip().split("\t")) == 2, "Invalid line"

    key, docs = line.strip().split("\t")

    index = []

    for doc in docs.strip("{}").split(", "):
        filename, count = doc.split("=")
        entry = {"filename": filename, "count": int(count)}
        index.append(entry)

    return {"word": key, "docs": index}


def convert_file(
    input_path: str,
    collection_name: str,
    db_name: str = "test",
    n_jobs: int = 4,
    host: str = "localhost",
    port: int = 27017,
) -> None:
    with open(input_path, "r") as f, MongoClient(host=host, port=port) as client:
        lines = f.readlines()

        with tqdm_joblib(
            tqdm(desc="Written", total=len(lines), unit="word")
        ) as progress_bar:
            converted_lines = joblib.Parallel(n_jobs=n_jobs)(
                joblib.delayed(convert_line)(line)
                for line in lines
                if len(line.strip().split("\t")) == 2
            )

            for converted_line in converted_lines:
                db = client[db_name]
                collection = db[collection_name]
                collection.insert_one(converted_line)


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Fast insert inverted index into MongoDB.",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )

    parser.add_argument("input", type=str, help="inverted index file")

    parser.add_argument(
        "-c",
        "--collection",
        action="store",
        type=str,
        default=argparse.SUPPRESS,
        help="mongo collection",
        required=True,
    )

    parser.add_argument(
        "-db",
        "--database",
        action="store",
        type=str,
        help="mongo db",
        default="test",
    )

    parser.add_argument(
        "-pl",
        "--parallelism",
        action="store",
        type=int,
        help="number of jobs for joblib",
        default=4,
    )

    parser.add_argument(
        "--host",
        action="store",
        type=str,
        help="mongo host",
        default="localhost",
    )

    parser.add_argument(
        "-p", "--port", action="store", type=int, help="mongo port", default=27017
    )

    args = parser.parse_args()

    try:
        convert_file(
            input_path=args.input,
            db_name=args.database,
            collection_name=args.collection,
            n_jobs=args.parallelism,
            host=args.host,
            port=args.port,
        )
    except Exception as e:
        print(e.args[0], file=sys.stderr)


if __name__ == "__main__":
    main()
