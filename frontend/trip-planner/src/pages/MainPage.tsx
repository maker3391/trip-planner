import Header from "../components/layout/Header";
import Sidebar from "../components/layout/Sidebar";
import "./MainPage.css";

export default function MainPage() {
  return (
    <div className="main-page">
      <Header />
      <div className="main-page-body">
        <Sidebar />
        <main className="map-area">
          <div className="map-placeholder">지도 영역</div>
        </main>
      </div>
    </div>
  );
}