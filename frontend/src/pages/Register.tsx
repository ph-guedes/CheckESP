import '../globals.css'
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { api } from "../services/apiService";
import { toast } from "sonner";

export default function Register() {
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleRegister(e: React.FormEvent) {  
    setLoading(true); 
    const toastId = toast.loading("Criando conta...");
    e.preventDefault();

    const fullName = `${firstName.trim()} ${lastName.trim()}`;

    const data = {
      name: fullName,
      email,
      password
    };

    try {
      const response = await api.post("/auth/register", data);
      toast.dismiss(toastId);
      toast.success("Conta criada com sucesso!");
      console.log(response.data);
      setLoading(false);
    } catch (error) {
      toast.dismiss(toastId);
      toast.error("Erro ao fazer login.");
      console.error("Erro ao registrar:", error);
      setLoading(false);
    }
  }

  return (
    <main>
      <div className="flex flex-col bg-white rounded-md h-full max-w-[450px] items-center p-10 shadow-md">
        <h1 className="text-2xl font-bold">Criar Conta</h1>
        <form onSubmit={handleRegister} className="max-w-md mx-auto p-4 space-y-4">
            <Input
              type="text" 
              placeholder="Nome"
              value={firstName}
              onChange={e => setFirstName(e.target.value)}
              required
            />
            <Input
              type="text"
              placeholder="Sobrenome"
              value={lastName}
              onChange={e => setLastName(e.target.value)}
              required
            />
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
            disabled={ loading } 
            className={`w-full p-2 rounded text-white ${loading ? "bg-gray-300" : "bg-zinc-600"}`} >
            { loading ? "Carregando..." : "Criar Conta" }
            </Button>
        </form>
      </div>
    </main>
  );
}