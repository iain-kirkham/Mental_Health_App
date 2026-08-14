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
      className={`flex flex-col items-center p-4 min-h-20 flex-1 transition-all duration-300 hover:scale-105 hover:shadow-md ${
        selected ? `${colorClass} border-2 scale-110 shadow-lg` : "border-2 border-border hover:border-ring"
      }`}
      onClick={() => onSelect(value)}
      aria-label={ariaLabel ?? `Select mood: ${label}`}
      aria-pressed={selected}
      disabled={disabled}
    >
      <div className={`text-3xl transition-transform duration-300 ${selected ? "scale-125" : "scale-100 opacity-70"}`}>
        {icon}
      </div>
      <span className={`text-xs mt-2 font-medium ${selected ? "font-bold" : ""}`}>{label}</span>
    </Button>
  );
}

