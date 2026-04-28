import { useEffect, useState } from 'react';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import { ChevronLeft, ChevronRight } from "lucide-react";
import AltRouteOutlinedIcon from "@mui/icons-material/AltRouteOutlined";
import '../layout/Sidebar.css';

export default function SideListPanel({
  isCollapsed, setIsCollapsed, path, setPath, selectedIdx, setSelectedIdx,
  pinColor, setPinColor, selectedPinColor, setSelectedPinColor, lineColor, setLineColor,
  showLines, setShowLines, handleDeletePin, reConnectAll, isReadOnly = false,
}: any) {
  const [showContent, setShowContent] = useState(!isCollapsed);

  useEffect(() => {
    if (isCollapsed) {
      setShowContent(false);
      return;
    }

    const timer = window.setTimeout(() => {
      setShowContent(true);
    }, 220);

    return () => window.clearTimeout(timer);
  }, [isCollapsed]);

  return (
    <div style={{
      position: 'absolute',
      top: '9px',
      left: '5px',
      zIndex: 10,
      background: '#eef2f7',
      padding: '10px',
      borderRadius: '20px',
      boxShadow: '0 10px 40px rgba(0,0,0,0.1)',
      width: isCollapsed ? '52px' : '340px',
      height: isCollapsed ? '52px' : 'auto',
      transition: 'width 0.25s ease',
      boxSizing: 'border-box',
      overflow: 'hidden'
    }}>
      <div style={{
        display: 'flex',
        justifyContent: isCollapsed ? 'center' : 'space-between',
        alignItems: 'center',
        height: '32px',
        marginBottom: showContent ? '20px' : 0,
        transition: 'margin-bottom 0.15s ease'
      }}>
        {showContent && (
          <h3
            style={{
              display: "flex",
              alignItems: "center",
              gap: "8px",
              margin: 0,
              fontSize: "18px",
              lineHeight: "32px",
              whiteSpace: "nowrap",
            }}
          >
            <AltRouteOutlinedIcon style={{ fontSize: "22px" }} />
            여행 경로
          </h3>
        )}

        <button
          type="button"
          className="sidebar-toggle"
          onClick={() => setIsCollapsed(!isCollapsed)}
          aria-label={isCollapsed ? "여행 경로 펼치기" : "여행 경로 접기"}
        >
          {isCollapsed ? (
            <ChevronRight size={20} strokeWidth={2} />
          ) : (
            <ChevronLeft size={20} strokeWidth={2} />
          )}
        </button>
      </div>

      {showContent && (
        <div>
          {/* 색상 변경 - readOnly면 숨김 */}
          {!isReadOnly && (
            <div style={{ background: '#fcfcfc', border: '1px solid #f0f0f0', borderRadius: '12px', padding: '15px', marginBottom: '15px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '15px', gap: '10px' }}>
                <ColorPicker label="기본 핀" value={pinColor} onChange={setPinColor} />
                <ColorPicker label="선택 핀" value={selectedPinColor} onChange={setSelectedPinColor} />
                <ColorPicker label="경로 선" value={lineColor} onChange={setLineColor} />
              </div>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button onClick={() => setShowLines(!showLines)} style={{ flex: 1, padding: '8px', fontSize: '11px', cursor: 'pointer', background: '#fff', border: '1px solid #ddd', borderRadius: '6px' }}>
                  {showLines ? "선 숨기기" : "선 보이기"}
                </button>
              </div>
            </div>
          )}

          {!isReadOnly && (
            <button
              onClick={handleDeletePin}
              disabled={selectedIdx === null}
              style={{
                width: '100%', padding: '12px', borderRadius: '10px', marginBottom: '10px',
                background: selectedIdx !== null ? '#fff2f0' : '#f5f5f5',
                color: selectedIdx !== null ? '#ff4d4f' : '#ccc',
                border: '1px solid #ffa39e', fontSize: '13px', fontWeight: 'bold',
                cursor: selectedIdx !== null ? 'pointer' : 'default'
              }}
            >
              선택된 핀 삭제
            </button>
          )}

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
                <div
                  {...provided.droppableProps}
                  ref={provided.innerRef}
                  style={{ maxHeight: '250px', overflowY: 'auto' }}
                >
                  {path.map((p: any, i: number) => (
                    <Draggable key={`${i}-${p.lat}`} draggableId={`${i}-${p.lat}`} index={i}>
                      {(provided) => (
                        <div
                          ref={provided.innerRef}
                          {...provided.draggableProps}
                          {...(!isReadOnly ? provided.dragHandleProps : {})}
                          onClick={() => setSelectedIdx(i)}
                          style={{
                            ...provided.draggableProps.style,
                            display: 'flex',
                            alignItems: 'center',
                            padding: '12px',
                            marginBottom: '8px',
                            borderRadius: '12px',
                            background: selectedIdx === i ? '#fff1f0' : '#f9f9f9',
                            border: selectedIdx === i ? '1px solid #ff4d4f' : '1px solid transparent'
                          }}
                        >
                          <span style={{
                            width: '25px',
                            fontSize: '12px',
                            fontWeight: 'bold',
                            color: selectedIdx === i ? '#ff4d4f' : '#999'
                          }}>
                            {i + 1}
                          </span>

                          <div style={{
                            flex: 1,
                            fontSize: '13px',
                            whiteSpace: 'nowrap',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis'
                          }}>
                            {p.name}
                          </div>
                        </div>
                      )}
                    </Draggable>
                  ))}
                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </DragDropContext>

          {/* 전체 초기화 - readOnly면 숨김 */}
          {!isReadOnly && (
            <button
              onClick={() => {
                if (confirm("초기화 하시겠습니까?")) {
                  setPath([]);
                  setSelectedIdx(null);
                }
              }}
              style={{ marginTop: '15px', width: '100%', padding: '12px', borderRadius: '10px', background: '#333', color: '#fff', border: 'none', fontSize: '13px', cursor: 'pointer' }}
            >
              전체 초기화
            </button>
          )}
        </div>
      )}
    </div>
  );
}

function ColorPicker({ label, value, onChange }: any) {
  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      gap: '5px',
      flex: 1
    }}>
      <label style={{
        fontSize: '10px',
        color: '#888',
        fontWeight: 'bold'
      }}>
        {label}
      </label>

      <input
        type="color"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        style={{
          border: 'none',
          width: '30px',
          height: '25px',
          cursor: 'pointer',
          background: 'transparent'
        }}
      />
    </div>
  );
}