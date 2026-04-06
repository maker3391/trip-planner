import { useEffect, useState } from "react";
import Header from "../components/layout/Header";
import Sidebar from "../components/layout/Sidebar";
import GuidePopup from "../components/guide/GuidePopup";
import MyMapApp, { PlacePoint, Connection } from "./MyMapApp"; 
import "./MainPage.css";

export default function MainPage() {
  const [openGuidePopup, setOpenGuidePopup] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [path, setPath] = useState<PlacePoint[]>([]);
  const [connections, setConnections] = useState<Connection[]>([]);

  const getPointId = (p: PlacePoint) => 
    `${p.lat.toFixed(6)}_${p.lng.toFixed(6)}`;

  const handleSaveToBackend = async () => {
    if (path.length === 0) {
      alert("저장할 경로가 없습니다!");
      return;
    }

    const requestData = {
      nodes: path.map(p => ({
        id: getPointId(p),
        name: p.name,
        lat: p.lat,
        lng: p.lng,
        address: p.address
      })),
      edges: connections.map(conn => ({
        fromId: getPointId(path[conn.from]),
        toId: getPointId(path[conn.to])
      }))
    };

    try {
      const response = await fetch('http://localhost:8080/api/travel/save', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData),
      });

      if (response.ok) {
        alert("✅ 여행 경로가 저장되었습니다!");
      } else {
        alert("❌ 저장 실패");
      }
    } catch (error) {
      console.error(error);
      alert("서버 연결 오류");
    }
  };

  useEffect(() => {
    const today = new Date().toISOString().split("T")[0];
    const hiddenDate = localStorage.getItem("hideGuidePopupDate");
    if (hiddenDate !== today) setOpenGuidePopup(true);
  }, []);

  return (
    <div className="main-page">
      <Header />
      
      <div className="main-page-body" style={{ display: 'flex', height: 'calc(100vh - 60px)' }}>
        <Sidebar onSearch={setSearchKeyword} />
        
        <main className="map-area" style={{ flexGrow: 1, position: 'relative' }}>
          
          {/* --- 수정된 저장 버튼 (왼쪽 하단 배치) --- */}
          <button 
            onClick={handleSaveToBackend}
            style={{
              position: 'absolute',
              bottom: '30px',      // 하단에서 30px 띄움
              left: '30px',        // 왼쪽에서 30px 띄움
              zIndex: 100,         // 지도보다 위에 보이도록
              padding: '14px 28px',
              backgroundColor: '#1a1a1a', // 좀 더 세련된 블랙 톤
              color: 'white',
              border: 'none',
              borderRadius: '12px',
              fontWeight: 'bold',
              boxShadow: '0 4px 20px rgba(0,0,0,0.3)',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '10px',
              transition: 'all 0.2s'
            }}
            onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#333'}
            onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#1a1a1a'}
          >
            <span style={{ fontSize: '18px' }}>💾</span> 저장하기
          </button>

          <div className="map-placeholder" style={{ width: '100%', height: '100%' }}>
            <MyMapApp 
              searchKeyword={searchKeyword}
              path={path}
              setPath={setPath}
              connections={connections}
              setConnections={setConnections}
            />
          </div>
        </main>
      </div>

      <GuidePopup open={openGuidePopup} onClose={() => setOpenGuidePopup(false)} />
    </div>
  );
}