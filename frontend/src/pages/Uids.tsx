import { useEffect, useState } from 'react'
import { Table, TableHeader, TableRow, TableHead, TableBody, TableCell } from '../components/ui/table'
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '../components/ui/card'
import { api } from '../services/apiService'
import { toast } from 'sonner'
import { Button } from '../components/ui/button'
import { Delete, PlusCircle } from 'lucide-react'
import { MainLayout } from '@/components/layout/MainLayout'
import { Pagination, PaginationContent, PaginationItem, PaginationNext, PaginationPrevious } from '@/components/ui/pagination'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '../components/ui/dialog'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'

export function Uids() {
  const [currentPage, setCurrentPage] = useState(1)
  const [currentPage2, setCurrentPage2] = useState(1)
  const [open, setOpen] = useState(false)
  const [uids, setUids] = useState<string[]>([])
  const [users, setUsers] = useState<{ id: number; name: string }[]>([])
  const [selectedUid, setSelectedUid] = useState("")
  const [selectedUserId, setSelectedUserId] = useState("")
  const [uidUsers, setUidUsers] = useState<{ uid: string, user: { name: string } }[]>([])
  const [uidLogs, setUidLogs] = useState<{ uid: string, timestamp: string , status: string}[]>([])
  const [loading, setLoading] = useState(false)

  const itemsPerPage = 6

  const totalPages = Math.ceil(uidLogs.length / itemsPerPage)
  const paginatedLogs = uidLogs.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  )
  
  const totalPages2 = Math.ceil(uidUsers.length / itemsPerPage)
  const paginatedUids = uidUsers.slice(
    (currentPage2 - 1) * itemsPerPage,
    currentPage2 * itemsPerPage
  )

  useEffect(() => {
    loadData()
    loadData2()
  }, [])

  useEffect(() => {
    if (open) {
      api.get("/uid-users/logs").then((res) => {
        const logs: { uid: string; status: string }[] = res.data || []

        const desconhecidosUnicos = Array.from(
          new Set(
            logs
              .filter((log) => log.status === "Desconhecido")
              .map((log) => log.uid)
          )
        )

        setUids(desconhecidosUnicos)
      })

      api.get("/users").then((res) => {
        setUsers(res.data || [])
      })
    }
  }, [open])


  async function loadData() {
    setLoading(true)
    try {
      const [logsRes] = await Promise.all([
        api.get('/uid-users/logs')
      ])
      setUidLogs(logsRes.data)
      setCurrentPage(1)
    } catch {
      toast.error('Erro ao carregar dados de UID.')
    } finally {
      setLoading(false)
    }
  }

  async function loadData2() {
    setLoading(true)
    try {
      const [uidsRes] = await Promise.all([
        api.get('/uid-users')
      ])
      setUidUsers(uidsRes.data)
      setCurrentPage2(1)
    } catch {
      toast.error('Erro ao carregar dados de UID.')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async () => {
    if (!selectedUid || !selectedUserId) return
    await api.post(`http://localhost:8081/api/uid-users?uid=${selectedUid}&userId=${selectedUserId}`)
    toast.success('UID criada com sucesso.')
    setOpen(false)
    setSelectedUid("")
    setSelectedUserId("")
  }

  async function handleDeleteLog(uid: string) {
    const confirmed = window.confirm(`Remover o log da UID ${uid}?`)
    if (!confirmed) return

    try {
      await api.delete(`/uid-logs/${uid}`)
      toast.success('Log removido com sucesso!')
      loadData()
    } catch {
      toast.error('Erro ao remover log.')
    }
  }

  return (
    <MainLayout>
    <div className='p-6 max-w-5xl mx-auto space-y-6'>
      <h1 className='text-3xl font-bold'>UIDs</h1>

      <Card className='w-4xl'>
        <CardHeader>
          <div className='flex justify-between items-center'>
          <CardTitle>UIDs Associadas a Usuários</CardTitle>
          <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
              <Button className='w-5 h-5 mr-2 ' variant="ghost">
                <PlusCircle />
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Novo UID</DialogTitle>
                <DialogDescription>Adicionar UID à um usuário.</DialogDescription>
              </DialogHeader>
              <form>
                <div className="flex flex-col-2 space-y-4 justify-between">
                  <Select onValueChange={setSelectedUid}>
                    <SelectTrigger>
                      <SelectValue placeholder="Selecione um UID" />
                    </SelectTrigger>
                    <SelectContent>
                      {uids.map((uid) => (
                        <SelectItem key={uid} value={uid}>
                          {uid}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>

                  <Select onValueChange={setSelectedUserId}>
                    <SelectTrigger>
                      <SelectValue placeholder="Selecione um usuário" />
                    </SelectTrigger>
                    <SelectContent>
                      {users.map((user) => (
                        user?.id != null && user?.name ? (
                          <SelectItem key={user.id} value={user.id.toString()}>
                            {user.name}
                          </SelectItem>
                        ) : null
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <DialogFooter>
                  <Button onClick={handleSubmit} disabled={!selectedUid || !selectedUserId}>
                    Vincular
                  </Button>
                </DialogFooter>
              </form>
            </DialogContent>
          </Dialog>
          </div>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>UID</TableHead>
                <TableHead>Nome</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {uidUsers.length === 0 && (
                <TableRow>
                  <TableCell colSpan={2} className="text-center">Nenhuma UID associada.</TableCell>
                </TableRow>
              )}
              {paginatedUids.map((u) => (
                <TableRow key={u.uid}>
                  <TableCell>{u.uid}</TableCell>
                  <TableCell>{u.user?.name}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
        <CardFooter className="flex justify-center">
          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  onClick={() => setCurrentPage2((prev) => Math.max(prev - 1, 1))}
                  className={currentPage === 1 ? 'pointer-events-none opacity-50' : ''}
                />
              </PaginationItem>

              {Array.from({ length: totalPages2 }, (_, i) => (
                <PaginationItem key={i}>
                  <button
                    onClick={() => setCurrentPage2(i + 1)}
                    className={`px-3 py-1 rounded-md text-sm ${
                      currentPage === i + 1
                        ? 'bg-zinc-800 text-white'
                        : 'hover:bg-zinc-200'
                    }`}
                  >
                    {i + 1}
                  </button>
                </PaginationItem>
              ))}

              <PaginationItem>
                <PaginationNext
                  onClick={() => setCurrentPage2((prev) => Math.min(prev + 1, totalPages2))}
                  className={currentPage === totalPages2 ? 'pointer-events-none opacity-50' : ''}
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </CardFooter>
      </Card>

      {/* Lista de logs */}
      <Card className='w-4xl'>
        <CardHeader>
          <CardTitle>Logs de UIDs Lidas</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>UID</TableHead>
                <TableHead>Data</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {uidLogs.length === 0 && (
                <TableRow>
                  <TableCell colSpan={3} className="text-center">Nenhum log encontrado.</TableCell>
                </TableRow>
              )}
              {paginatedLogs.map((log) => (
                <TableRow key={log.uid}>
                  <TableCell>{log.uid}</TableCell>
                  <TableCell>{new Date(log.timestamp).toLocaleString('pt-BR')}</TableCell>
                  <TableCell>{log.status}</TableCell>
                  <TableCell>
                    <Button
                      variant='ghost'
                      className='w-4 h-4 p-0'
                      onClick={() => handleDeleteLog(log.uid)}
                    >
                      <Delete />
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
        <CardFooter className="flex justify-center">
          <Pagination>
            <PaginationContent>
              <PaginationItem>
                <PaginationPrevious
                  onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
                  className={currentPage === 1 ? 'pointer-events-none opacity-50' : ''}
                />
              </PaginationItem>

              {Array.from({ length: totalPages }, (_, i) => (
                <PaginationItem key={i}>
                  <button
                    onClick={() => setCurrentPage(i + 1)}
                    className={`px-3 py-1 rounded-md text-sm ${
                      currentPage === i + 1
                        ? 'bg-zinc-800 text-white'
                        : 'hover:bg-zinc-200'
                    }`}
                  >
                    {i + 1}
                  </button>
                </PaginationItem>
              ))}

              <PaginationItem>
                <PaginationNext
                  onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages))}
                  className={currentPage === totalPages ? 'pointer-events-none opacity-50' : ''}
                />
              </PaginationItem>
            </PaginationContent>
          </Pagination>
        </CardFooter>
      </Card>
    </div>
    </MainLayout>
  )
}
