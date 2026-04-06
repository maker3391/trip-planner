import { useNavigate } from "react-router-dom";
import "./ProfileDropdown.css";

interface UserInfo {
  id: number;
  email: string;
  name?: string;
  nickname?: string;
  phone?: string;
  profileImage?: string;
  role?: string;
  status?: string;
}

interface ProfileDropdownProps {
  user: UserInfo | null;
  onClose: () => void;
}

export default function ProfileDropdown({
  user,
  onClose,
}: ProfileDropdownProps) {
  const navigate = useNavigate();

  const email = user?.email || "";
  const name = user?.name || "";
  const nickname = user?.nickname || "";
  const profileImage = user?.profileImage || "";

  const displayMainName = nickname || name || "";
  const displaySubName = nickname && name ? name : "";

  const fallbackInitial =
    (displayMainName && displayMainName.charAt(0)) ||
    (email && email.charAt(0).toUpperCase()) ||
    "?";

  const handleEditProfile = () => {
    onClose();
    navigate("/mypage");
  };

  return (
    <div className="profile-dropdown">
      <span className="profile-dropdown-badge">PROFILE</span>
      <h3 className="profile-dropdown-title">내 프로필</h3>

      <div className="profile-summary-card">
        <div className="profile-image-box">
          {profileImage ? (
            <img
              src={profileImage}
              alt="프로필 이미지"
              className="profile-image"
            />
          ) : (
            <div className="profile-image-placeholder">{fallbackInitial}</div>
          )}
        </div>

        <div className="profile-summary-text">
          <div className="profile-summary-nickname">{displayMainName}</div>
          {displaySubName && (
            <div className="profile-summary-name">{displaySubName}</div>
          )}
        </div>
      </div>

      <div className="profile-info-list">
        <div className="profile-info-row">
          <span className="profile-info-label">이메일</span>
          <strong className="profile-info-value">{email}</strong>
        </div>

        <div className="profile-info-row">
          <span className="profile-info-label">이름</span>
          <strong className="profile-info-value">{name || nickname}</strong>
        </div>
      </div>

      <div className="profile-dropdown-actions">
        <button className="profile-cancel-btn" onClick={onClose}>
          닫기
        </button>
        <button className="profile-edit-btn" onClick={handleEditProfile}>
          프로필 수정
        </button>
      </div>
    </div>
  );
}