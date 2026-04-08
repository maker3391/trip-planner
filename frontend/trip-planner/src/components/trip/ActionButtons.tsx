import React from 'react';
//MainPage에서 ActionButtons 컴포넌트를 분리하여 별도의 파일로 관리
interface ActionButtonsProps {
  onOpenSaveModal: () => void;
  isLoading: boolean;
}

export default function ActionButtons({ onOpenSaveModal, isLoading }: ActionButtonsProps) {
  return (
    <div style={{ position: 'absolute', bottom: '30px', left: '30px', zIndex: 100, display: 'flex', gap: '12px' }}>
      <button
        onClick={onOpenSaveModal}
        style={{ padding: '14px 28px', backgroundColor: '#1a1a1a', color: 'white', border: 'none', borderRadius: '12px', fontWeight: 'bold', cursor: 'pointer', boxShadow: '0 4px 15px rgba(0,0,0,0.2)' }}
      >
        <span>💾</span> 계획 저장
      </button>
      {isLoading && <span style={{ alignSelf: 'center', fontSize: '12px', color: '#666' }}>데이터 로딩 중...</span>}
    </div>
  );
}