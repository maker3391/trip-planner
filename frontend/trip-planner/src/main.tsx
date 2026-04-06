// import React from "react";
// import ReactDOM from "react-dom/client";
// import App from "./App";

// ReactDOM.createRoot(document.getElementById("root")!).render(
//   <React.StrictMode>
//     <App />
//   </React.StrictMode>
// );
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

// 1. React Query 도구들을 임포트합니다.
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// 2. 상황실(QueryClient) 객체를 하나 생성합니다.
const queryClient = new QueryClient();

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    {/* 3. 앱 전체를 QueryClientProvider로 감싸고, 위에서 만든 상황실을 연결합니다. */}
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </React.StrictMode>
);