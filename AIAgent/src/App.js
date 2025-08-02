import React, { useState } from 'react';
import './App.css';

// Enhanced JSON viewer with better key-value formatting
const JsonViewer = ({ data }) => {
  const [copied, setCopied] = useState(false);
  
  const handleCopy = () => {
    navigator.clipboard.writeText(JSON.stringify(data, null, 2));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const renderValue = (value, key = null) => {
    if (value === null) {
      return <span className="json-null">null</span>;
    }
    if (typeof value === 'boolean') {
      return <span className="json-boolean">{value.toString()}</span>;
    }
    if (typeof value === 'number') {
      return <span className="json-number">{value}</span>;
    }
    if (typeof value === 'string') {
      // Format dates nicely
      if (key && (key.toLowerCase().includes('date') || key.toLowerCase().includes('time')) && value.includes('T')) {
        const date = new Date(value);
        return (
          <span className="json-string" title={value}>
            "{date.toLocaleString()}"
          </span>
        );
      }
      return <span className="json-string">"{value}"</span>;
    }
    if (Array.isArray(value)) {
      return (
        <div className="json-array">
          <span className="json-bracket">[</span>
          {value.map((item, index) => (
            <div key={index} className="json-array-item">
              {renderObject(item, 1)}
              {index < value.length - 1 && <span className="json-comma">,</span>}
            </div>
          ))}
          <span className="json-bracket">]</span>
        </div>
      );
    }
    if (typeof value === 'object') {
      return renderObject(value, 1);
    }
    return <span>{JSON.stringify(value)}</span>;
  };

  const renderObject = (obj, level = 0) => {
    if (!obj || typeof obj !== 'object') {
      return renderValue(obj);
    }

    const entries = Object.entries(obj);
    return (
      <div className="json-object" style={{ marginLeft: `${level * 20}px` }}>
        <span className="json-brace">{'{'}</span>
        {entries.map(([key, value], index) => (
          <div key={key} className="json-property">
            <div className="json-key-value">
              <span className="json-key">"{key}"</span>
              <span className="json-colon">: </span>
              {renderValue(value, key)}
              {index < entries.length - 1 && <span className="json-comma">,</span>}
            </div>
          </div>
        ))}
        <span className="json-brace">{'}'}</span>
      </div>
    );
  };

  return (
    <div className="json-display">
      <button 
        className="copy-button" 
        onClick={handleCopy}
        title="Copy to clipboard"
      >
        {copied ? '✓ Copied!' : '📋 Copy'}
      </button>
      <div className="json-content">
        {renderValue(data)}
      </div>
    </div>
  );
};

// Loading animation component
const LoadingSpinner = () => (
  <div className="loading-spinner"></div>
);

function App() {
  const [searchTerm, setSearchTerm] = useState('');
  const [jsonData, setJsonData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSearch = async () => {
    if (!searchTerm.trim()) {
      setError('Please enter a search term');
      return;
    }

    setLoading(true);
    setError('');
    setJsonData(null);

    try {
      // Call the real API endpoint
      const apiUrl = `http://localhost:8080/api/aiagent/input?inputPrompt=${encodeURIComponent(searchTerm)}`;
      
      const response = await fetch(apiUrl, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        // Add timeout to prevent hanging requests
        signal: AbortSignal.timeout(30000) // 30 seconds timeout
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      
      setJsonData(data);
    } catch (err) {
      console.error('API Error:', err);
      
      let errorMessage = 'Failed to fetch data from API.';
      
      if (err.name === 'TypeError' && err.message.includes('fetch')) {
        errorMessage = 'Unable to connect to API server. Please ensure the server is running on http://localhost:8080';
      } else if (err.name === 'AbortError') {
        errorMessage = 'Request timed out. Please try again.';
      } else if (err.message.includes('HTTP')) {
        errorMessage = `API Error: ${err.message}`;
      } else if (err.name === 'SyntaxError') {
        errorMessage = 'Invalid JSON response from API.';
      }
      
      setError(errorMessage);
      
      // Optional: Show detailed error in development
      if (process.env.NODE_ENV === 'development') {
        console.log('Detailed error:', err);
        setJsonData({
          error: true,
          error_type: err.name,
          error_message: err.message,
          debug_info: {
            stack: err.stack,
            cause: err.cause
          }
        });
      }
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const clearResults = () => {
    setJsonData(null);
    setError('');
  };

  return (
    <div className="App">
      <div className="container">
        <h1>🤖 AI Agent Explorer</h1>
        
        <div className="search-section">
          <div className="search-container">
            <div className="search-input-container">
              <span className="search-icon">🔍</span>
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="Ask the AI Agent anything..."
                className="search-input"
                disabled={loading}
              />
            </div>
            <button 
              onClick={handleSearch}
              className="search-button"
              disabled={loading || !searchTerm.trim()}
            >
              {loading && <LoadingSpinner />}
              {loading ? 'Processing...' : '🤖 Ask AI'}
            </button>
            {jsonData && (
              <button 
                onClick={clearResults}
                className="search-button"
                style={{background: 'linear-gradient(135deg, #e74c3c 0%, #c0392b 100%)'}}
              >
                Clear
              </button>
            )}
          </div>
          
          {error && <div className="error-message">{error}</div>}
          
          {/* API Status Indicator */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '10px',
            marginTop: '15px',
            fontSize: '14px',
            color: '#666'
          }}>
            <span>🔗</span>
            <span>Connected to: http://localhost:8080/api/aiagent/input</span>
          </div>
        </div>

        {jsonData && (
          <div className="results-section">
            <h2>Response Details</h2>
            <div className="response-display">
              <div className="response-grid">
                {Array.isArray(jsonData) ? (
                  <div className="array-value">
                    <div className="array-header">Response Array ({jsonData.length} records):</div>
                    {jsonData.map((item, index) => (
                      <div key={index} className="array-item">
                        <span className="array-index">{index + 1}</span>
                        {typeof item === 'object' ? (
                          <div className="nested-object">
                            {Object.entries(item).filter(([nestedKey, nestedValue]) => 
                              nestedValue !== null && 
                              nestedValue !== '' && 
                              nestedValue !== 0 && 
                              nestedValue !== '0'
                            ).map(([nestedKey, nestedValue]) => (
                              <div key={nestedKey} className="nested-item">
                                <strong className="nested-key">{nestedKey}:</strong>
                                <span className="nested-value">{String(nestedValue)}</span>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <span>{String(item)}</span>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  Object.entries(jsonData).map(([key, value]) => (
                    <div key={key} className="response-item">
                      <div className="response-key">
                        <span className="key-icon">🔹</span>
                        <span className="key-text">{key.replace(/([A-Z])/g, ' $1').replace(/_/g, ' ').trim()}</span>
                      </div>
                      <div className="response-value">
                        {typeof value === 'string' ? (
                          <div className="string-value" dangerouslySetInnerHTML={{ __html: value.replace(/\n/g, '<br/>') }} />
                        ) : typeof value === 'number' ? (
                          <span className="number-value">{value}</span>
                        ) : typeof value === 'boolean' ? (
                          <span className={`boolean-value ${value ? 'true' : 'false'}`}>{value.toString()}</span>
                        ) : value === null ? (
                          <span className="null-value">null</span>
                        ) : Array.isArray(value) ? (
                          <div className="array-value">
                            <div className="array-header">Array ({value.length} items):</div>
                            {value.map((item, index) => (
                              <div key={index} className="array-item">
                                <span className="array-index">{index + 1}</span>
                                {typeof item === 'object' ? (
                                  <div className="nested-object">
                                    {Object.entries(item).filter(([nestedKey, nestedValue]) => 
                                      nestedValue !== null && 
                                      nestedValue !== '' && 
                                      nestedValue !== 0 && 
                                      nestedValue !== '0'
                                    ).map(([nestedKey, nestedValue]) => (
                                      <div key={nestedKey} className="nested-item">
                                        <strong>{nestedKey}:</strong> {String(nestedValue)}
                                      </div>
                                    ))}
                                  </div>
                                ) : (
                                  <span>{String(item)}</span>
                                )}
                              </div>
                            ))}
                          </div>
                        ) : typeof value === 'object' ? (
                          <div className="object-value">
                            {Object.entries(value).filter(([nestedKey, nestedValue]) => 
                              nestedValue !== null && 
                              nestedValue !== '' && 
                              nestedValue !== 0 && 
                              nestedValue !== '0'
                            ).map(([nestedKey, nestedValue]) => (
                              <div key={nestedKey} className="nested-item">
                                <strong className="nested-key">{nestedKey}:</strong>
                                <span className="nested-value">{String(nestedValue)}</span>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <span className="unknown-value">{String(value)}</span>
                        )}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}

export default App;