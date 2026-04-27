import { BrowserRouter } from "react-router-dom";
import Router from "./components/router/Router";
import { Toaster } from "react-hot-toast";

function App() {
  return (
    <BrowserRouter
      future={{
        v7_startTransition: true,
        v7_relativeSplatPath: true,
      }}
    >
      <Toaster
        position="bottom-center"
        reverseOrder={false}
        toastOptions={{
          duration: 7000,
        }}
      />
      <Router />
    </BrowserRouter>
  );
}

export default App;