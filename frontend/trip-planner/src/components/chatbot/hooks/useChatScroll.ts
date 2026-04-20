import { useEffect, useRef, useState } from "react";
import type {
  UIEvent,
  TouchEvent as ReactTouchEvent,
  WheelEvent as ReactWheelEvent,
} from "react";

interface UseChatScrollReturn {
  chatBodyRef: React.RefObject<HTMLDivElement | null>;
  showScrollToBottomButton: boolean;
  maybeAutoScrollToBottom: () => void;
  enableAutoScrollAndJumpToBottom: () => void;
  resetAutoScroll: () => void;
  handleBodyScroll: (_event: UIEvent<HTMLDivElement>) => void;
  handleWheelCapture: (event: ReactWheelEvent<HTMLDivElement>) => void;
  handleTouchStart: (event: ReactTouchEvent<HTMLDivElement>) => void;
  handleTouchMove: (event: ReactTouchEvent<HTMLDivElement>) => void;
}

export default function useChatScroll(open: boolean): UseChatScrollReturn {
  const [showScrollToBottomButton, setShowScrollToBottomButton] =
    useState(false);

  const chatBodyRef = useRef<HTMLDivElement | null>(null);
  const autoScrollEnabledRef = useRef(true);
  const touchStartYRef = useRef<number | null>(null);

  const isNearBottom = (): boolean => {
    const container = chatBodyRef.current;
    if (!container) return true;

    const threshold = 72;
    const distanceFromBottom =
      container.scrollHeight - container.scrollTop - container.clientHeight;

    return distanceFromBottom <= threshold;
  };

  const scrollToBottom = (behavior: ScrollBehavior = "auto"): void => {
    const container = chatBodyRef.current;
    if (!container) return;

    container.scrollTo({
      top: container.scrollHeight,
      behavior,
    });
  };

  const disableAutoScroll = (): void => {
    autoScrollEnabledRef.current = false;
    setShowScrollToBottomButton(true);
  };

  const enableAutoScrollAndJumpToBottom = (): void => {
    autoScrollEnabledRef.current = true;
    setShowScrollToBottomButton(false);
    scrollToBottom("smooth");
  };

  const resetAutoScroll = (): void => {
    autoScrollEnabledRef.current = true;
    setShowScrollToBottomButton(false);
  };

  const maybeAutoScrollToBottom = (): void => {
    if (autoScrollEnabledRef.current) {
      scrollToBottom("auto");
    } else {
      setShowScrollToBottomButton(true);
    }
  };

  const handleBodyScroll = (_event: UIEvent<HTMLDivElement>): void => {
    if (isNearBottom()) {
      autoScrollEnabledRef.current = true;
      setShowScrollToBottomButton(false);
    }
  };

  const handleWheelCapture = (event: ReactWheelEvent<HTMLDivElement>): void => {
    if (event.deltaY < 0) {
      disableAutoScroll();
    }
  };

  const handleTouchStart = (event: ReactTouchEvent<HTMLDivElement>): void => {
    touchStartYRef.current = event.touches[0]?.clientY ?? null;
  };

  const handleTouchMove = (event: ReactTouchEvent<HTMLDivElement>): void => {
    const currentY = event.touches[0]?.clientY;
    const startY = touchStartYRef.current;

    if (currentY == null || startY == null) return;

    if (currentY > startY) {
      disableAutoScroll();
    }
  };

  useEffect(() => {
    if (!open) return;

    requestAnimationFrame(() => {
      scrollToBottom("auto");
    });
  }, [open]);

  return {
    chatBodyRef,
    showScrollToBottomButton,
    maybeAutoScrollToBottom,
    enableAutoScrollAndJumpToBottom,
    resetAutoScroll,
    handleBodyScroll,
    handleWheelCapture,
    handleTouchStart,
    handleTouchMove,
  };
}