import { create } from "zustand";
import { normalizeCategoryKey } from "@/lib/channel-color";

const STORAGE_KEY = "category-colors-v1";

function readStorage(): Record<string, number> {
  if (typeof window === "undefined") return {};
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as Record<string, number>) : {};
  } catch {
    return {};
  }
}

function writeStorage(overrides: Record<string, number>) {
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(overrides));
  } catch {
    // Best-effort only (private browsing / storage full) - the override just won't survive reload.
  }
}

interface CategoryColorState {
  /** category key (normalized) -> palette index. Starts empty on both server and client so the
   * first render never mismatches; `hydrate` fills it in from localStorage once mounted. */
  overrides: Record<string, number>;
  hydrated: boolean;
  hydrate: () => void;
  setCategoryColorIndex: (category: string, index: number | null) => void;
}

export const useCategoryColorStore = create<CategoryColorState>((set, get) => ({
  overrides: {},
  hydrated: false,
  hydrate: () => {
    if (get().hydrated) return;
    set({ overrides: readStorage(), hydrated: true });
  },
  setCategoryColorIndex: (category, index) => {
    const key = normalizeCategoryKey(category);
    if (!key) return;
    const next = { ...get().overrides };
    if (index === null) delete next[key];
    else next[key] = index;
    set({ overrides: next });
    writeStorage(next);
  },
}));
