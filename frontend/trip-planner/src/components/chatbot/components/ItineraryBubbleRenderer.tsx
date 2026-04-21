interface ItineraryDay {
  day: string;
  route: string;
}

interface ItineraryBubbleRendererProps {
  content: string;
}

const parseItineraryContent = (content: string): {
  title: string;
  days: ItineraryDay[];
} => {
  const lines = content
    .split("\n")
    .map((line) => line.trim())
    .filter(Boolean);

  const title = lines[0] ?? "여행 일정";
  const days: ItineraryDay[] = [];

  for (let i = 1; i < lines.length; i += 2) {
    const dayLine = lines[i];
    const routeLine = lines[i + 1];

    if (!dayLine) continue;

    if (dayLine.startsWith("Day")) {
      days.push({
        day: dayLine,
        route: routeLine ?? "",
      });
    }
  }

  return { title, days };
};

export default function ItineraryBubbleRenderer({
  content,
}: ItineraryBubbleRendererProps) {
  const { title, days } = parseItineraryContent(content);

  return (
    <div className="itinerary-card">
      <div className="itinerary-card__header">
        <p className="itinerary-card__title">{title}</p>
      </div>

      <div className="itinerary-card__body">
        {days.map((item, index) => (
          <div className="itinerary-day" key={`${item.day}-${index}`}>
            <div className="itinerary-day__badge">{item.day}</div>
            <p className="itinerary-day__route">{item.route}</p>
          </div>
        ))}
      </div>
    </div>
  );
}