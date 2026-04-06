import { Navigate } from "react-router-dom";

interface ProtectedRouteProps {
  children: JSX.Element;
}

export default function ProtectedRoute({
  children,
}: ProtectedRouteProps) {
  const isLoggedIn = localStorage.getItem("isLoggedIn") === "true";
  const accessToken = localStorage.getItem("accessToken");

  if (!isLoggedIn || !accessToken || accessToken === "undefined") {
    alert("로그인 후 이용 가능합니다.");
    return <Navigate to="/login" replace />;
  }

  return children;
}