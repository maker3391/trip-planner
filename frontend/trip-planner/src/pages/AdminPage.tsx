import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../components/layout/Header";
import toast from "react-hot-toast";
import "./AdminPage.css";

export default function AdminPage() {
  const [keyword, setKeyword] = useState("");
  const navigate = useNavigate();

  const handleSearch = () => {
    toast.error("회원 검색 API가 아직 구현되지 않았습니다.");
  };

  return (
    <>
      <Header />

      <main className="admin-page">
        <section className="admin-container">

          <section className="admin-header-card">
            <div className="admin-header-top">
              <div>
                <span className="admin-badge">ADMIN</span>
                <h1>관리자 페이지</h1>
                <p>회원 정보를 확인하고 서비스 이용 상태를 관리할 수 있습니다.</p>
              </div>

              <button
                type="button"
                className="admin-cs-button"
                onClick={() => navigate("/admin/cs")}
              >
                1:1 문의 관리
              </button>
            </div>
          </section>

          <div className="admin-panel">
            <div className="admin-panel-header">
              <h2>회원 관리</h2>
            </div>

            <div className="admin-search-row">
              <input
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="이메일 또는 닉네임으로 검색"
              />
              <button type="button" onClick={handleSearch}>
                검색
              </button>
            </div>

            <div className="admin-table-wrap">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>이메일</th>
                    <th>닉네임</th>
                    <th>권한</th>
                    <th>상태</th>
                    <th>관리</th>
                  </tr>
                </thead>

                <tbody>
                  <tr>
                    <td colSpan={6} className="admin-empty">
                      회원 목록 조회 API가 아직 없어 실제 목록을 불러올 수 없습니다.
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

        </section>
      </main>
    </>
  );
}