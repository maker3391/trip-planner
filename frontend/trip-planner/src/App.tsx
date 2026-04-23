import { BrowserRouter } from "react-router-dom";
import Router from "./components/router/Router";

function App() {
  return (
    <BrowserRouter
      future={{
        v7_startTransition: true,
        v7_relativeSplatPath: true,
      }}
    >
      <Router />
    </BrowserRouter>
  );
}

export default App;