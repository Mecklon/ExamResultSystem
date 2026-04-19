import { useState, useRef, useEffect } from "react";
import api from "../api/api";

const useDeleteFetch = (initialValue = null) => {
  const [state, setState] = useState(initialValue);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const controllerRef = useRef();

  const fetch = async (query, params = {}, skipAuth = false, headers = {}) => {
    if (controllerRef.current) controllerRef.current.abort();
    controllerRef.current = new AbortController();

    setLoading(true);
    setError(null);

    try {
      const res = await api.delete(query, {
        skipAuth: skipAuth,
        signal: controllerRef.current.signal,
        headers,
        params, // passing query parameters like ?departmentName=XYZ
      });
      setState(res.data);
      return res.data;
    } catch (error) {
      if (error.name === 'CanceledError' || error.code === 'ERR_CANCELED') {
        console.log("Request canceled by cleanup");
        return;
      }
      if (error.response) {
        console.log("server error");
        setError(error.response.status + " " + (error.response.data?.error || error.response.data || "Server Error"));
      } else if (error.request) {
        console.log("Network error");
        setError("Network error - unable to reach the server");
      } else {
        console.log("something went wrong");
        setError(error.message || "Something went wrong");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    return () => {
      if (controllerRef.current) controllerRef.current.abort();
    };
  }, []);

  return { state, setState, error, loading, fetch };
};

export default useDeleteFetch;
