import { describe, expect, it, vi } from "vitest";
import { act, renderHook } from "@testing-library/react";
import { useDebouncedCommit } from "./useDebouncedCommit";

describe("useDebouncedCommit", () => {
  it("commits once after the delay, not once per call", () => {
    vi.useFakeTimers();
    try {
      const { result } = renderHook(() => useDebouncedCommit(600));
      const onCommit = vi.fn();

      act(() => {
        result.current("a", onCommit);
        result.current("ab", onCommit);
        result.current("abc", onCommit);
      });
      expect(onCommit).not.toHaveBeenCalled();

      act(() => vi.advanceTimersByTime(600));

      expect(onCommit).toHaveBeenCalledTimes(1);
      expect(onCommit).toHaveBeenCalledWith("abc");
    } finally {
      vi.useRealTimers();
    }
  });

  it("does not commit if unmounted before the delay elapses", () => {
    vi.useFakeTimers();
    try {
      const { result, unmount } = renderHook(() => useDebouncedCommit(600));
      const onCommit = vi.fn();

      act(() => result.current("a", onCommit));
      unmount();
      act(() => vi.advanceTimersByTime(600));

      expect(onCommit).not.toHaveBeenCalled();
    } finally {
      vi.useRealTimers();
    }
  });

  it("lets each call bind its own onCommit, closing over per-call state", () => {
    vi.useFakeTimers();
    try {
      const { result } = renderHook(() => useDebouncedCommit(600));
      const first = vi.fn();
      const second = vi.fn();

      act(() => result.current("x", first));
      act(() => vi.advanceTimersByTime(600));
      act(() => result.current("y", second));
      act(() => vi.advanceTimersByTime(600));

      expect(first).toHaveBeenCalledWith("x");
      expect(second).toHaveBeenCalledWith("y");
    } finally {
      vi.useRealTimers();
    }
  });
});
