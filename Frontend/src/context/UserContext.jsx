import React, { createContext, useContext } from 'react';
import useLocalState from '../Hooks/useLocalState';

const UserContext = createContext();

export const UserProvider = ({ children }) => {
  const [user, setUser] = useLocalState("user", null);

  const login = (userData, token) => {
    localStorage.setItem("JwtToken", token);
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem("JwtToken");
    setUser(null);
  };

  return (
    <UserContext.Provider value={{ user, login, logout }}>
      {children}
    </UserContext.Provider>
  );
};

export const useUser = () => {
  return useContext(UserContext);
};
