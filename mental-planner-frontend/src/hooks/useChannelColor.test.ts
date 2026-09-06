import { describe, expect, it } from "vitest";
import { resolveChannelColor } from "./useChannelColor";
import { CHANNEL_COLORS, defaultChannelColorIndex } from "@/lib/channel-color";

describe("resolveChannelColor", () => {
  it("falls back to the deterministic hash-based color when there's no override", () => {
    const expectedIndex = defaultChannelColorIndex("work");
    const resolved = resolveChannelColor({}, "work");
    expect(resolved.index).toBe(expectedIndex);
    expect(resolved.isOverride).toBe(false);
    expect(resolved).toMatchObject(CHANNEL_COLORS[expectedIndex]);
  });

  it("uses a manual override when one is set for the category", () => {
    const resolved = resolveChannelColor({ work: 3 }, "work");
    expect(resolved.index).toBe(3);
    expect(resolved.isOverride).toBe(true);
    expect(resolved).toMatchObject(CHANNEL_COLORS[3]);
  });

  it("normalizes category casing/whitespace before looking up an override", () => {
    const resolved = resolveChannelColor({ work: 3 }, "  Work  ");
    expect(resolved.index).toBe(3);
    expect(resolved.isOverride).toBe(true);
  });

  it("treats a null/undefined/empty category as 'default'", () => {
    const expectedIndex = defaultChannelColorIndex("default");
    expect(resolveChannelColor({}, null).index).toBe(expectedIndex);
    expect(resolveChannelColor({}, undefined).index).toBe(expectedIndex);
    expect(resolveChannelColor({}, "").index).toBe(expectedIndex);
  });
});
