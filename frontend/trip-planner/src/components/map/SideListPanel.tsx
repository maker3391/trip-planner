import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';

export default function SideListPanel({ 
  isCollapsed, setIsCollapsed, path, setPath, selectedIdx, setSelectedIdx, 
  pinColor, setPinColor, selectedPinColor, setSelectedPinColor, lineColor, setLineColor,
  showLines, setShowLines, showAllMemos, setShowAllMemos, handleDeletePin, reConnectAll 
}: any) {
  return (
    <div style={{
      position: 'absolute', top: '20px', left: '20px', zIndex: 10, background: 'white', padding: '24px', 
      borderRadius: '20px', boxShadow: '0 10px 40px rgba(0,0,0,0.1)', width: isCollapsed ? '60px' : '340px', transition: '0.3s'
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: isCollapsed ? 0 : '20px' }}>
        {!isCollapsed && <h3 style={{ margin: 0, fontSize: '18px' }}>📍 여행 경로</h3>}
        <button onClick={() => setIsCollapsed(!isCollapsed)} style={{ cursor: 'pointer', border: 'none', background: '#f5f5f5', borderRadius: '50%', width: '30px', height: '30px' }}>
          {isCollapsed ? "▶" : "◀"}
        </button>
      </div>

      {!isCollapsed && (
        <>
          <div style={{ background: '#fcfcfc', border: '1px solid #f0f0f0', borderRadius: '12px', padding: '15px', marginBottom: '15px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px', gap: '10px' }}>
              <ColorPicker label="기본 핀" value={pinColor} onChange={setPinColor} />
              <ColorPicker label="선택 핀" value={selectedPinColor} onChange={setSelectedPinColor} />
              <ColorPicker label="경로 선" value={lineColor} onChange={setLineColor} />
            </div>
            <div style={{ display: 'flex', gap: '8px' }}>
              <button onClick={() => setShowLines(!showLines)} style={{ flex: 1, padding: '8px', fontSize: '11px', cursor: 'pointer', background: '#fff', border: '1px solid #ddd', borderRadius: '6px' }}>{showLines ? "선 숨기기" : "선 보이기"}</button>
              
            </div>
          </div>

          <button 
            onClick={handleDeletePin}
            disabled={selectedIdx === null}
            style={{ width: '100%', padding: '12px', borderRadius: '10px', marginBottom: '10px', background: selectedIdx !== null ? '#fff2f0' : '#f5f5f5', color: selectedIdx !== null ? '#ff4d4f' : '#ccc', border: '1px solid #ffa39e', fontSize: '13px', fontWeight: 'bold', cursor: selectedIdx !== null ? 'pointer' : 'default' }}
          >선택된 핀 삭제</button>

          <DragDropContext onDragEnd={(result) => {
              if (!result.destination) return;
              const newPath = Array.from(path); 
              const [reorderedItem] = newPath.splice(result.source.index, 1);
              newPath.splice(result.destination.index, 0, reorderedItem);
              setPath(newPath); 
              reConnectAll(newPath); 
              setSelectedIdx(result.destination.index);
            }}>
            <Droppable droppableId="pathList">
              {(provided) => (
                <div {...provided.droppableProps} ref={provided.innerRef} style={{ maxHeight: '250px', overflowY: 'auto' }}>
                  {path.map((p: any, i: number) => (
                    <Draggable key={`${i}-${p.lat}`} draggableId={`${i}-${p.lat}`} index={i}>
                      {(provided) => (
                        <div ref={provided.innerRef} {...provided.draggableProps} {...provided.dragHandleProps} onClick={() => setSelectedIdx(i)} style={{ ...provided.draggableProps.style, display: 'flex', alignItems: 'center', padding: '12px', marginBottom: '8px', borderRadius: '12px', background: selectedIdx === i ? '#fff1f0' : '#f9f9f9', border: selectedIdx === i ? '1px solid #ff4d4f' : '1px solid transparent' }}>
                          <span style={{ width: '25px', fontSize: '12px', fontWeight: 'bold', color: selectedIdx === i ? '#ff4d4f' : '#999' }}>{i + 1}</span>
                          <div style={{ flex: 1, fontSize: '13px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{p.name}</div>
                        </div>
                      )}
                    </Draggable>
                  ))}
                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </DragDropContext>
          <button onClick={() => { if(confirm("초기화 하시겠습니까?")) { setPath([]); setSelectedIdx(null); }}} style={{ marginTop: '15px', width: '100%', padding: '12px', borderRadius: '10px', background: '#333', color: '#fff', border: 'none', fontSize: '13px', cursor: 'pointer' }}>전체 초기화</button>
        </>
      )}
    </div>
  );
}

function ColorPicker({ label, value, onChange }: any) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '5px', flex: 1 }}>
      <label style={{ fontSize: '10px', color: '#888', fontWeight: 'bold' }}>{label}</label>
      <input type="color" value={value} onChange={(e) => onChange(e.target.value)} style={{ border: 'none', width: '30px', height: '25px', cursor: 'pointer', background: 'transparent' }} />
    </div>
  );
}