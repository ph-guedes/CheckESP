import { Button } from "@/components/ui/button"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { cn } from "@/lib/utils"
import { Calendar } from "@/components/ui/calendar"
import { addDays, format } from "date-fns"
import { ptBR } from "date-fns/locale"
import { CalendarIcon } from "lucide-react"
import * as React from "react"
import { type DateRange } from "react-day-picker"


interface DateRangePickerProps extends React.HTMLAttributes<HTMLDivElement> {
  value?: DateRange | undefined
  onRangeChange?: (range: DateRange | undefined) => void
}
export default function DateRangePicker({
  className,
  value,
  onRangeChange,
  ...rest
}: DateRangePickerProps) {
  const [internalDate, setInternalDate] = React.useState<DateRange | undefined>(() =>
    value ?? { from: addDays(new Date(), -20), to: new Date() }
  )

  React.useEffect(() => {
    if (value !== undefined) {
      setInternalDate(value)
    }
  }, [value])

  function handleSelect(range: DateRange | undefined) {
    if (onRangeChange) {
      onRangeChange(range)
    } else {
      setInternalDate(range)
    }
  }

  const date = value ?? internalDate

  return (
    <div className={cn("grid gap-2", className)} {...rest}>
      <Popover>
        <PopoverTrigger asChild>
          <Button
            id="date"
            variant="outline"
            className={cn(
              "w-[300px] justify-start text-left font-normal",
              !date && "text-muted-foreground"
            )}
          >
            <CalendarIcon className="mr-2 h-4 w-4" />
            {date?.from ? (
              date.to ? (
                <>
                  {format(date.from, "dd/MM/yyyy", { locale: ptBR })} -{" "}
                  {format(date.to, "dd/MM/yyyy", { locale: ptBR })}
                </>
              ) : (
                format(date.from, "dd/MM/yyyy", { locale: ptBR })
              )
            ) : (
              <span>Escolha um intervalo</span>
            )}
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto p-0" align="start">
          <Calendar
            autoFocus
            mode="range"
            defaultMonth={date?.from}
            selected={date}
            onSelect={handleSelect}
            numberOfMonths={2}
          />
        </PopoverContent>
      </Popover>
    </div>
  )
}