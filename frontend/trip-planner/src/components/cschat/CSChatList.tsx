import { useEffect, useState } from 'react';
import { getMyCSRooms, CSRoomResponse } from '../api/csChat';
import './css/CSChatList.css';

interface CSChatListProps {
    onBack: () => void;
    onEnterRoom: (room: CSRoomResponse) => void;
    onCreateNew: (title: string) => void; 
}

export default function CSChatList({ onBack, onEnterRoom, onCreateNew }: CSChatListProps) {
    const [myRooms, setMyRooms] = useState<CSRoomResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isCreating, setIsCreating] = useState(false);
    const [newTitle, setNewTitle] = useState("");

    useEffect(() => {
        const fetchRooms = async () => {
            try {
                const rooms = await getMyCSRooms();
                setMyRooms(rooms);
            } catch (error) {
                console.error("문의 내역을 불러오지 못했습니다.", error);
            } finally {
                setIsLoading(false);
            }
        };
        fetchRooms();
    }, []);

    const handleCreateSubmit = () => {
        if (!newTitle.trim()) {
            alert("문의하실 제목을 입력해주세요!");
            return;
        }
        onCreateNew(newTitle); 
    };

    return (
        <div className="cs-list-container">
            <div className="cs-list-header">
                <button className="cs-chat-back-btn" onClick={onBack}>⬅️</button>
                <h3 className="cs-list-title">나의 문의 내역</h3>
            </div>

            <div className="cs-list-content">
                {isLoading ? (
                    <p className="cs-list-empty">불러오는 중...</p>
                ) : myRooms.length === 0 ? (
                    <p className="cs-list-empty">진행 중인 문의 내역이 없습니다.</p>
                ) : (
                    myRooms.map((room) => (
                        <div key={room.id} className="cs-list-item" onClick={() => onEnterRoom(room)}>
                            <div className="cs-list-item-info">
                                <h4>{room.title}</h4>
                                <p>방 번호: {room.id} | {new Date(room.createdAt).toLocaleDateString()}</p>
                            </div>
                            <div className="cs-list-status">
                                {room.status === 'WAITING' ? '대기중' : '진행중'}
                            </div>
                        </div>
                    ))
                )}
            </div>

            <div className="cs-list-create-area">
                {isCreating ? (
                    <>
                        <input 
                            className="cs-list-title-input"
                            type="text" 
                            placeholder="예) 결제 취소 요청합니다" 
                            value={newTitle}
                            onChange={(e) => setNewTitle(e.target.value)}
                            onKeyPress={(e) => e.key === 'Enter' && handleCreateSubmit()}
                        />
                        <button className="cs-list-submit-btn" onClick={handleCreateSubmit}>
                            상담 시작하기
                        </button>
                    </>
                ) : (
                    <button className="cs-list-new-btn" onClick={() => setIsCreating(true)}>
                        + 새 문의하기
                    </button>
                )}
            </div>
        </div>
    );
}