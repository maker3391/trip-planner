import Header from "../components/layout/Header.tsx";
import "./CommunityPage.css";

export default function CommunityPage() {
    return (
        <div className="community-page">
            <Header />
            
            <div className="community-content">
                <h1>커뮤니티 페이지</h1>
                <p>여기는 커뮤니티 페이지입니다. 여행 계획을 공유하고 다른 사람들과 소통하세요!</p>
                {/* 커뮤니티 게시글 리스트, 게시글 작성 폼 등 추가 예정 */}

                <ul>
                    <li>게시글 1</li>
                    <li>게시글 2</li>
                    <li>게시글 3</li>
                </ul>
            </div>
        </div>
    );
}