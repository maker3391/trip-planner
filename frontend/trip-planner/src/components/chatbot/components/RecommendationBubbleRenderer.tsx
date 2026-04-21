import type { RecommendationPayload } from "../types/chatUi";

interface RecommendationBubbleRendererProps {
  payload: RecommendationPayload;
}

export default function RecommendationBubbleRenderer({
  payload,
}: RecommendationBubbleRendererProps) {
  const { title, summary, items, kind } = payload;

  const emptyText =
    kind === "restaurant"
      ? "추천할 맛집 정보를 찾지 못했습니다."
      : "추천할 숙소 정보를 찾지 못했습니다.";

  return (
    <div className="recommendation-card">
      <div className="recommendation-card__header">
        <p className="recommendation-card__title">{title}</p>
        {summary && <p className="recommendation-card__summary">{summary}</p>}
      </div>

      <div className="recommendation-card__body">
        {items.length === 0 ? (
          <p className="recommendation-card__empty">{emptyText}</p>
        ) : (
          items.map((item) => (
            <div className="recommendation-item" key={item.id}>
              <div className="recommendation-item__top">
                <div className="recommendation-item__title-row">
                  <p className="recommendation-item__name">{item.title}</p>

                  <div className="recommendation-item__actions">
                    {kind === "restaurant" && item.category && (
                      <span className="recommendation-item__category">
                        {item.category}
                      </span>
                    )}

                    {item.link && (
                      <a
                        className="recommendation-item__icon-link"
                        href={item.link}
                        target="_blank"
                        rel="noreferrer"
                        aria-label={`${item.title} 상세 보기`}
                        title="상세 보기"
                      >
                        <svg
                          className="recommendation-item__icon"
                          viewBox="0 0 24 24"
                          fill="none"
                          aria-hidden="true"
                        >
                          <circle
                            cx="10.5"
                            cy="10.5"
                            r="5.5"
                            stroke="currentColor"
                            strokeWidth="1.8"
                          />
                          <path
                            d="M15 15L19.5 19.5"
                            stroke="currentColor"
                            strokeWidth="1.8"
                            strokeLinecap="round"
                          />
                        </svg>
                      </a>
                    )}
                  </div>
                </div>
              </div>

              {item.description && (
                <p className="recommendation-item__description">
                  {item.description}
                </p>
              )}

              {item.address && (
                <p className="recommendation-item__address">{item.address}</p>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}