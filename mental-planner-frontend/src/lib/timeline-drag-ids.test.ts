import { describe, expect, it } from "vitest";
import { makeGridId, makeQueueId, makeResizeId, parseDragId } from "./timeline-drag-ids";

describe("timeline drag ids", () => {
  it("round-trips a queue id", () => {
    expect(parseDragId(makeQueueId(5))).toEqual({ kind: "queue", taskId: 5 });
  });

  it("round-trips a grid id", () => {
    expect(parseDragId(makeGridId(12))).toEqual({ kind: "grid", taskId: 12 });
  });

  it("round-trips a resize id", () => {
    expect(parseDragId(makeResizeId(7))).toEqual({ kind: "resize", taskId: 7 });
  });

  it("throws on an id with no recognized prefix", () => {
    expect(() => parseDragId("42")).toThrow("Invalid drag id: 42");
  });

  it("throws on an unrelated droppable id", () => {
    expect(() => parseDragId("timeline-grid")).toThrow(/Invalid drag id/);
  });
});
