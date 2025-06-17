// src/components/ui/sidebar-toggle.tsx
import { ChevronLeft } from "lucide-react";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";

interface SidebarToggleProps {
  isOpen: boolean;
  setIsOpen: () => void;
}

export function SidebarToggle({ isOpen, setIsOpen }: SidebarToggleProps) {
  return (
    <div className="absolute -right-4 top-4 z-10">
      <Button
        onClick={setIsOpen}
        className="rounded-md w-8 h-8"
        variant="outline"
        size="icon"
      >
        <ChevronLeft
          className={cn(
            "h-4 w-4 transition-transform ease-in-out duration-300",
            !isOpen && "rotate-180"
          )}
        />
      </Button>
    </div>
  );
}
