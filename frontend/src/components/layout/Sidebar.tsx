// Sidebar.tsx
import { Home, ListChecks, Users, Key, Settings, ListTodo } from "lucide-react"
import { NavLink } from "react-router-dom"
import { cn } from "@/lib/utils"
import { SidebarToggle } from "../ui/sidebar-toggle"
import { ThemeToggle } from "../ui/theme-toggle"

const links = [
  { to: "/", label: "Dashboard", icon: <Home className="w-4 h-5" /> },
  { to: "/checklists", label: "Checklists", icon: <ListChecks className="w-4 h-5" /> },
  { to: "/items", label: "Itens", icon: <ListTodo className="w-4 h-5" /> },
  { to: "/users", label: "Usuários", icon: <Users className="w-4 h-5" /> },
  { to: "/uids", label: "UIDs", icon: <Key className="w-4 h-5" /> },
  { to: "/config", label: "Configurações", icon: <Settings className="w-4 h-5" /> }
]

interface SidebarProps {
  isOpen: boolean
  setIsOpen: () => void
}

export function Sidebar({ isOpen, setIsOpen }: SidebarProps) {

        return (
        <aside className={cn(
        "bg-zinc-100 text-black dark:bg-zinc-950 dark:text-white w-64 fixed transition-all duration-300",
        !isOpen && "w-16"
        )}>

            <SidebarToggle isOpen={isOpen} setIsOpen={setIsOpen} />
            <div className="h-screen flex flex-col justify-between">
            <div>
            <div className="p-6 text-3xl font-bold border-b border-zinc-700">{isOpen ? "CheckESP" : "C"}</div>
            <nav className="flex flex-col gap-1 p-2">
                {links.map((link) => (
                <NavLink
                    key={link.to}
                    to={link.to}
                    className={({ isActive }) =>
                    cn(
                        "flex items-center gap-3 rounded-lg px-4 py-2 text-sm font-medium hover:bg-zinc-700 transition-all",
                        isActive && "bg-zinc-500"
                    )
                    }
                >
                    {link.icon}
                    {isOpen && link.label}
                </NavLink>
                ))}
            </nav>
            </div>
            <div className="p-4 flex justify-end">
            <ThemeToggle />
            </div>
        </div>
    </aside>
  )
}