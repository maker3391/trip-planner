import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { getMe } from "../api/auth";
import toast from "react-hot-toast";

interface AdminRouteProps {
  children: JSX.Element;
}

export default function AdminRoute({ children }: AdminRouteProps) {
  const [isLoading, setIsLoading] = useState(true);
  const [isAdmin, setIsAdmin] = useState(false);
  const [shouldRedirect, setShouldRedirect] = useState(false);

  useEffect(() => {
    const checkAdmin = async () => {
      const accessToken = localStorage.getItem("accessToken");

      if (!accessToken || accessToken === "undefined") {
        toast.error("로그인 후 이용 가능합니다.");
        setShouldRedirect(true);
        setIsLoading(false);
        return;
      }

      try {
        const user = await getMe();

        if (user.role === "ADMIN") {
          setIsAdmin(true);
        } else {
          toast.error("관리자만 접근 가능합니다.");
          setShouldRedirect(true);
        }
      } catch (error) {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        toast.error("인증이 만료되었습니다.");
        setShouldRedirect(true);
      } finally {
        setIsLoading(false);
      }
    };

    checkAdmin();
  }, []);

  if (isLoading) return null;

  if (shouldRedirect) {
    return <Navigate to="/" replace />;
  }

  return children;
}