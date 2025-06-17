import '../globals.css'
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { api } from "../services/apiService";
import { toast } from "sonner";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleLogin(e: React.FormEvent) {
    setLoading(true); 
    const toastId = toast.loading("Entrando...");
    e.preventDefault();

    const data = { email, password};

    try {
      const response = await api.post("/auth/login", data);
      toast.dismiss(toastId);
      toast.success("Login realizado com sucesso!");
      console.log(response.data);
      setLoading(false);
    } catch (error) {
      toast.dismiss(toastId);
      toast.error("Erro ao fazer login.");
      console.error("Erro ao entrar:", error);
      setLoading(false);
    }
  }

  return (
    <main>
      <div className="flex flex-col gap-6 bg-white items-center p-10">
        <h1 className='text-2xl font-bold' >Entrar</h1>
        <form onSubmit={handleLogin} className="max-w-md mx-auto p-4 space-y-4">
          <Input
            type="email"
            placeholder="Email"
            value={email}
            onChange={e => setEmail(e.target.value)}
            required
          />
          <Input
            type="password"
            placeholder="Senha"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />
          <Button
            type="submit"
            disabled={loading}
            className={`w-full p-2 rounded text-white ${loading ? "bg-gray-300" : "bg-zinc-600"}`} >
            {loading ? "Entrando..." : "Entrar"}
        </Button>
        </form>
      </div>
    </main>
  );
}