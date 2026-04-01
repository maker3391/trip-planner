import { useEffect, useState } from "react";
import Header from "../components/layout/Header";
import Sidebar from "../components/layout/Sidebar";
import GuidePopup from "../components/guide/GuidePopup";
import MyMapApp from "./MyMapApp";
import "./MainPage.css";

export default function MainPage() {
  const [openGuidePopup, setOpenGuidePopup] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");

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
      <div className="main-page-body" style={{ display: 'flex', height: 'calc(100vh - 60px)' }}> {/* Header 높이 제외 */}
        <Sidebar onSearch={setSearchKeyword} />
        <main className="map-area" style={{ flexGrow: 1, position: 'relative' }}>
          {/* 여기서 60vh를 주면 위아래에 연두색 여백이 남을 수 있습니다. 꽉 채우려면 100%가 좋습니다. */}
          <div className="map-placeholder" style={{ width: '100%', height: '100%' }}>

            <MyMapApp searchKeyword={searchKeyword}/>

          </div>
        </main>
      </div>

      <GuidePopup
        open={openGuidePopup}
        onClose={() => setOpenGuidePopup(false)}
      />
    </div>
  );
}