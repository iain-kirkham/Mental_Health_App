import React from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Calendar } from "@/components/ui/calendar";
import { CalendarIcon, Clock } from "lucide-react";

type Props = {
  date: Date | undefined;
  setDate: (d: Date | undefined) => void;
  time: string;
  setTime: (t: string) => void;
  isSubmitting: boolean;
  formatDate: (d: Date | undefined) => string;
};

export default function DateTimePickers({ date, setDate, time, setTime, isSubmitting, formatDate }: Props) {
  return (
    <div className="flex gap-2 flex-1">
      <Popover>
        <PopoverTrigger asChild>
          <Button variant="outline" className="flex justify-start items-center flex-1" disabled={isSubmitting} aria-label="Select date">
            <CalendarIcon className="mr-2 h-4 w-4 flex-shrink-0" />
            <span className="truncate">{formatDate(date)}</span>
          </Button>
        </PopoverTrigger>
          <PopoverContent
              data-slot="popover-content"
              className="w-auto p-0"
          >
              <Calendar mode="single" selected={date} onSelect={setDate} autoFocus className="rounded-lg border shadow-sm" />
          </PopoverContent>

      </Popover>

      <Popover>
        <PopoverTrigger asChild>
          <Button variant="outline" className="flex justify-start items-center flex-1" disabled={isSubmitting} aria-label="Select time">
            <Clock className="mr-2 h-4 w-4 flex-shrink-0" />
            <span className="truncate">{time}</span>
          </Button>
        </PopoverTrigger>
        <PopoverContent className="p-4 w-auto">
          <div className="space-y-2">
            <h4 className="font-medium">Select time</h4>
            <Input type="time" value={time} onChange={(e) => setTime(e.target.value)} disabled={isSubmitting} />
          </div>
        </PopoverContent>
      </Popover>
    </div>
  );
}
