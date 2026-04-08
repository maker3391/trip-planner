import React from 'react';
import LocalMemoEditor from './LocalMemoEditor';

export default function DetailPanel({ selectedIdx, path, setPath, onClose }: any) {
  if (selectedIdx === null || !path[selectedIdx]) return null;
  const point = path[selectedIdx];

  return (
    <div style={{ width: '360px', background: 'white', boxShadow: '-5px 0 20px rgba(0,0,0,0.05)', zIndex: 11, display: 'flex', flexDirection: 'column', height: '100%' }}>
      
      {/* 1. 최상단 닫기 버튼 영역 (새로 추가) */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', padding: '12px 16px 0 16px' }}>
        <button 
          onClick={onClose} 
          style={{ 
            border: 'none', 
            background: 'none', 
            cursor: 'pointer', 
            display: 'flex', 
            alignItems: 'center', 
            gap: '4px',
            color: '#999',
            fontSize: '13px',
            padding: '4px 8px',
            borderRadius: '4px',
            transition: 'background 0.2s'
          }}
          onMouseEnter={(e) => (e.currentTarget.style.background = '#f5f5f5')}
          onMouseLeave={(e) => (e.currentTarget.style.background = 'none')}
        >
          <span style={{ fontSize: '18px', lineHeight: '1' }}>×</span>
          <span>닫기</span>
        </button>
      </div>

      <div style={{ padding: '12px 24px 24px 24px', borderBottom: '1px solid #f0f0f0' }}>
        
        {/* 2. 장소 사진 표시 영역 (기존 내부 버튼 제거됨) */}
        {point.photos && point.photos.length > 0 ? (
          <div style={{ width: '100%', height: '180px', marginBottom: '15px', borderRadius: '12px', overflow: 'hidden' }}>
            <img src={point.photos[0]} alt="place" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          </div>
        ) : (
          <div style={{ width: '100%', height: '120px', backgroundColor: '#f5f5f5', borderRadius: '12px', marginBottom: '15px', display: 'flex', justifyContent: 'center', alignItems: 'center', color: '#ccc', fontSize: '12px' }}>
            이미지가 없습니다
          </div>
        )}

        <h3 style={{ margin: '0 0 8px 0', fontSize: '20px' }}>{point.name}</h3>
        <p style={{ fontSize: '13px', color: '#666', marginBottom: '15px' }}>{point.address}</p>
        
        <button 
          onClick={() => window.open(`https://map.naver.com/v5/search/${encodeURIComponent(point.name)}`, '_blank')} 
          style={{ width: '100%', padding: '10px', borderRadius: '8px', background: '#03C75A', color: 'white', border: 'none', fontSize: '13px', fontWeight: 'bold', cursor: 'pointer' }}
        >
          N 네이버 지도로 보기
        </button>
      </div>
      
      <div style={{ flex: 1, overflowY: 'auto', padding: '20px' }}>
        <label style={{ fontSize: '12px', fontWeight: 'bold', color: '#666' }}>상세 메모</label>
        
        <LocalMemoEditor 
          initialMemo={point.memo || ""} 
          
          onSave={(newMemo) => {
            // 입력이 끝났을 때(onBlur) 실행될 로직
            setPath((prev: any[]) => prev.map((p, i) => 
              i === selectedIdx ? { ...p, memo: newMemo } : p
            ));
          }} 
        />
      </div>
    </div>
  );
}