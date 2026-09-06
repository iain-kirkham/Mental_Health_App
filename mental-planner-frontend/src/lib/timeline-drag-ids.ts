export type DragIdKind = "queue" | "grid" | "resize";

export interface ParsedDragId {
  kind: DragIdKind;
  taskId: number;
}

const PREFIXES: Record<DragIdKind, string> = {
  queue: "queue-",
  grid: "grid-",
  resize: "resize-",
};

export function makeQueueId(taskId: number): string {
  return `${PREFIXES.queue}${taskId}`;
}

export function makeGridId(taskId: number): string {
  return `${PREFIXES.grid}${taskId}`;
}

export function makeResizeId(taskId: number): string {
  return `${PREFIXES.resize}${taskId}`;
}

/** Every draggable in the Today/timeline DndContext is produced by one of the make*Id functions
 * above, so an id that matches none of them is a wiring bug in a new drag source, not a runtime
 * case to handle gracefully - this throws rather than returning null. */
export function parseDragId(id: string): ParsedDragId {
  for (const kind of Object.keys(PREFIXES) as DragIdKind[]) {
    const prefix = PREFIXES[kind];
    if (id.startsWith(prefix)) {
      return { kind, taskId: Number(id.slice(prefix.length)) };
    }
  }
  throw new Error(`Invalid drag id: ${id}`);
}
