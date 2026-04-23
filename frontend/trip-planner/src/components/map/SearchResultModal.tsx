import "./SearchResultModal.css";
import { SearchPlace } from "../../types/searchPlace.ts";

interface SearchResultModalProps {
  open: boolean;
  keyword: string;
  results: SearchPlace[];
  onClose: () => void;
  onSelect: (place: SearchPlace) => void;
}

export default function SearchResultModal({
  open,
  keyword,
  results,
  onClose,
  onSelect,
}: SearchResultModalProps) {
  if (!open) return null;

  return (
    <div className="search-result-overlay" onClick={onClose}>
      <div
        className="search-result-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="search-result-header">
          <div>
            <h3>검색 결과</h3>
            <p>"{keyword}"에 대한 후보 장소입니다.</p>
          </div>

          <button
            type="button"
            className="search-result-close"
            onClick={onClose}
            aria-label="검색 결과 닫기"
          >
            
          </button>
        </div>

        <div className="search-result-body">
          {results.length === 0 ? (
            <div className="search-result-empty">검색 결과가 없습니다.</div>
          ) : (
            results.map((place, index) => (
              <button
                key={`${place.placeId || place.name}-${index}`}
                type="button"
                className="search-result-item"
                onClick={() => onSelect(place)}
              >
                <strong>{place.name}</strong>
                <span>{place.address}</span>
              </button>
            ))
          )}
        </div>
      </div>
    </div>
  );
}