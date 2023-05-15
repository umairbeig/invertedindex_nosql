import './App.css';
import React, { useState } from 'react';

const App = () => {
  const [word, setWord] = useState('');
  const [selectedValue, setSelectedValue] = useState('a');
  const [responses, setResponses] = useState([]);

  const handleWordChange = (event) => {
    setWord(event.target.value);
  };

  const handleDropdownChange = (event) => {
    setSelectedValue(event.target.value);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      const response = await fetch(`http://localhost:8080/api/search/${word}/${selectedValue}`);
      const data = await response.json();
     
      setResponses(data);
      
     
      // console.log(data);
    } catch (error) {
      console.error('Error:', error);
      console.log("word not found")
    }
  };
  const handleHomeClick = () => {
    window.location.href = 'http://localhost:3000';
  };

  const openFile = async (filePath) => {
    try {
      const response = await fetch(`http://localhost:8080/api/open/${filePath}/${selectedValue}`);
      const text = await response.text();

      // Open file contents in a new browser tab
      const newTab = window.open('', '_blank');
      newTab.document.write('<pre>' + text + '</pre>');
      newTab.document.close();
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className="container-fluid py-5 text-black  justify-content-center align-items-center text-white">
      <h1>Search the Word</h1>
      <div className="container-fluid py-5 text-white">
        <form onSubmit={handleSubmit} className="d-flex">
          <input type="text" value={word} onChange={handleWordChange} className="form-control me-2" placeholder="Enter word" />
          <select value={selectedValue} onChange={handleDropdownChange} className="form-select me-2">
            <option value="">Select Table</option>
            <option value="table1">bbc-news</option>
            <option value="table2">Wiki</option>
            <option value="table3">Books dataset</option>
          </select>
          <button className="btn btn-lg btn-dark" type="submit">Search</button>
          <button className="btn btn-lg btn-danger ms-2" onClick={handleHomeClick}>Home</button>
        </form>
      </div>
      <div className="row mt-4  container-fluid py-5 text-white d-flex justify-content-center align-items-center">
        <div className="col">
          <div className="card">
            <div className="card-body">
              <h5 className="card-title">Search Results</h5>
              <div className="table-responsive">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Filename</th>
                      <th>Count</th>
                    </tr>
                  </thead>
                  <tbody>
                    {responses.map((doc, index) => (
                      <tr key={index}>
                        <td onClick={() => openFile(doc.filename)}>{doc.filename}</td>
                        <td>{doc.count}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default App;
