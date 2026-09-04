import React from "react";
import { Button } from "@/components/ui/button";

type Props<T> = {
  value: T;
  label: string;
  icon: React.ReactNode;
  colorClass: string;
  selected: boolean;
  onSelect: (v: T) => void;
  disabled?: boolean;
  ariaLabel?: string;
};

export default function MoodOption<T>({ value, label, icon, colorClass, selected, onSelect, disabled, ariaLabel }: Props<T>) {
  return (
    <Button
      variant="outline"
      className={`flex flex-col items-center gap-2 p-4 min-h-20 flex-1 border-2 transition-colors duration-200 ${
        selected ? colorClass : "border-border hover:border-ring"
      }`}
      onClick={() => onSelect(value)}
      aria-label={ariaLabel ?? `Select mood: ${label}`}
      aria-pressed={selected}
      disabled={disabled}
    >
      <div className={selected ? "" : "opacity-60"}>{icon}</div>
      <span className={`text-xs ${selected ? "font-semibold" : "font-medium"}`}>{label}</span>
    </Button>
  );
}

