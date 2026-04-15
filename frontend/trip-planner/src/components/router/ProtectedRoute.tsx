import { Navigate } from "react-router-dom";

interface ProtectedRouteProps {
  children: JSX.Element;
}

export default function ProtectedRoute({
  children,
}: ProtectedRouteProps) {
  const accessToken = localStorage.getItem("accessToken");

  if (!accessToken || accessToken === "undefined") {
    alert("로그인 후 이용 가능합니다.");
    return <Navigate to="/login" replace />;
  }

  return children;
}