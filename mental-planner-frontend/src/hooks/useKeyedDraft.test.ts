import { describe, expect, it } from "vitest";
import { act, renderHook } from "@testing-library/react";
import { useKeyedDraft } from "./useKeyedDraft";

describe("useKeyedDraft", () => {
  it("returns the fallback until a value is set for the current key", () => {
    const { result } = renderHook(() => useKeyedDraft<number, string>(1, "fallback"));

    expect(result.current[0]).toBe("fallback");
  });

  it("returns the set value while the key stays the same", () => {
    const { result } = renderHook(() => useKeyedDraft<number, string>(1, "fallback"));

    act(() => result.current[1]("edited"));

    expect(result.current[0]).toBe("edited");
  });

  it("falls back instead of leaking the previous key's value when the key changes", () => {
    const { result, rerender } = renderHook(
      ({ key, fallback }: { key: number; fallback: string }) => useKeyedDraft<number, string>(key, fallback),
      { initialProps: { key: 1, fallback: "fallback-1" } }
    );

    act(() => result.current[1]("edited-for-1"));
    expect(result.current[0]).toBe("edited-for-1");

    rerender({ key: 2, fallback: "fallback-2" });
    expect(result.current[0]).toBe("fallback-2");

    // Switching back to key 1 re-reveals what was stored for it, since a write for key 2 never
    // happened - the hook only ever tracks the single most recent write, keyed.
    rerender({ key: 1, fallback: "fallback-1" });
    expect(result.current[0]).toBe("edited-for-1");
  });

  it("reset() clears the stored value and falls back immediately", () => {
    const { result } = renderHook(() => useKeyedDraft<number, string>(1, "fallback"));

    act(() => result.current[1]("edited"));
    expect(result.current[0]).toBe("edited");

    act(() => result.current[2]());
    expect(result.current[0]).toBe("fallback");
  });

  it("treats a null key as nothing to write to", () => {
    const { result } = renderHook(() => useKeyedDraft<number, string>(null, "fallback"));

    act(() => result.current[1]("edited"));

    expect(result.current[0]).toBe("fallback");
  });
});
