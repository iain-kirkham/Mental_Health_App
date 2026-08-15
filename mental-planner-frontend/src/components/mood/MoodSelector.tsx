import React from "react";
import type { MoodOption as MoodOptionType } from "@/types";
import MoodOption from "./MoodOption";
import { Frown, Smile, Meh } from "lucide-react";

export const MOOD_OPTIONS: MoodOptionType[] = [
	{
		value: 1,
		icon: <Frown size={32} strokeWidth={2.5} />,
		label: "Very Bad",
		color: "bg-destructive/15 hover:bg-destructive/25 border-destructive text-destructive",
	},
	{
		value: 2,
		icon: <Frown size={32} />,
		label: "Bad",
		color: "bg-chart-4/15 hover:bg-chart-4/25 border-chart-4 text-chart-4",
	},
	{
		value: 3,
		icon: <Meh size={32} />,
		label: "Okay",
		color: "bg-chart-3/15 hover:bg-chart-3/25 border-chart-3 text-chart-3",
	},
	{
		value: 4,
		icon: <Smile size={32} />,
		label: "Good",
		color: "bg-chart-2/15 hover:bg-chart-2/25 border-chart-2 text-chart-2",
	},
	{
		value: 5,
		icon: <Smile size={32} strokeWidth={2.5} />,
		label: "Very Good",
		color: "bg-chart-5/15 hover:bg-chart-5/25 border-chart-5 text-chart-5",
	},
];

type Props = {
	selectedMood: number | null;
	setSelectedMood: (v: number | null) => void;
	isSubmitting: boolean;
};

export default function MoodSelector({
	selectedMood,
	setSelectedMood,
	isSubmitting,
}: Props) {
	return (
		<div
			className="flex justify-between items-center gap-2 py-2"
			role="group"
			aria-label="Mood selection"
		>
			{MOOD_OPTIONS.map((mood) => (
				<MoodOption
					key={mood.value}
					value={mood.value}
					label={mood.label}
					icon={mood.icon}
					colorClass={mood.color}
					selected={selectedMood === mood.value}
					onSelect={(v) => setSelectedMood(v)}
					disabled={isSubmitting}
				/>
			))}
		</div>
	);
}
