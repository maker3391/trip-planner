import { useEffect, useState } from "react";
import Header from "../components/layout/Header";
import Sidebar from "../components/layout/Sidebar";
import GuidePopup from "../components/guide/GuidePopup";
import "./MainPage.css";

export default function MainPage() {
  const [openGuidePopup, setOpenGuidePopup] = useState(false);

  useEffect(() => {
    const today = new Date().toISOString().split("T")[0];
    const hiddenDate = localStorage.getItem("hideGuidePopupDate");

    if (hiddenDate !== today) {
      setOpenGuidePopup(true);
    }
  }, []);

  return (
    <div className="main-page">
      <Header />
      <div className="main-page-body">
        <Sidebar />
        <main className="map-area">
          <div className="map-placeholder">지도 영역</div>
        </main>
      </div>

      <GuidePopup
        open={openGuidePopup}
        onClose={() => setOpenGuidePopup(false)}
      />
    </div>
  );
}