import { useEffect, useRef, useState } from "react";

interface TypingTextProps {
  content: string;
  speed?: number;
  animate?: boolean;
  onTypingProgress?: () => void;
  onTypingEnd?: () => void;
}

const renderMessageLines = (content: string) => {
  return content.split("\n").map((line, index) => (
    <p key={index}>{line || "\u00A0"}</p>
  ));
};

export default function TypingText({
  content,
  speed = 14,
  animate = true,
  onTypingProgress,
  onTypingEnd,
}: TypingTextProps) {
  const [displayedText, setDisplayedText] = useState(animate ? "" : content);
  const progressCallbackRef = useRef<typeof onTypingProgress>(onTypingProgress);
  const endCallbackRef = useRef<typeof onTypingEnd>(onTypingEnd);

  useEffect(() => {
    progressCallbackRef.current = onTypingProgress;
  }, [onTypingProgress]);

  useEffect(() => {
    endCallbackRef.current = onTypingEnd;
  }, [onTypingEnd]);

  useEffect(() => {
    if (!animate) {
      setDisplayedText(content);
      return;
    }

    let index = 0;
    setDisplayedText("");

    const timer = window.setInterval(() => {
      index += 1;
      const nextText = content.slice(0, index);

      setDisplayedText(nextText);
      progressCallbackRef.current?.();

      if (index >= content.length) {
        window.clearInterval(timer);
        endCallbackRef.current?.();
      }
    }, speed);

    return () => window.clearInterval(timer);
  }, [content, speed, animate]);

  return <>{renderMessageLines(displayedText)}</>;
}