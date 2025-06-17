
import { type ReactNode, useState } from "react"
import { Sidebar } from "./Sidebar"
import { cn } from "@/lib/utils"

export function MainLayout({ children }: { children: ReactNode }) {
  const [isOpen, setIsOpen] = useState(true)

  return (
    <div className="flex min-h-screen bg-zinc-50">
      <Sidebar isOpen={isOpen} setIsOpen={() => setIsOpen(!isOpen)} />

        <main
          className={cn(
            "flex-1 p-4 transition-all duration-300",
            isOpen ? "ml-64" : "ml-16",
            "bg-zinc-50 dark:bg-zinc-900 text-black dark:text-white"
          )}>
        {children}
      </main>
    </div>
  )
}
