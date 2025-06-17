import '../globals.css'
import { useState, useEffect } from 'react'
import { Button } from '../components/ui/button'
import { Input } from '../components/ui/input'
import { Label } from '../components/ui/label'
import { Search, PlusCircle, Delete } from 'lucide-react'
import { Pagination, PaginationContent, PaginationItem, PaginationPrevious, PaginationNext } from '@/components/ui/pagination'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../components/ui/table'
import { Dialog, DialogClose, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '../components/ui/dialog'
import CreatableSelect from 'react-select/creatable'
import { api } from '../services/apiService'
import { toast } from 'sonner'
import { Card, CardContent, CardFooter, CardHeader } from '@/components/ui/card'
import { MainLayout } from '@/components/layout/MainLayout'

export function Items() {
  const [loading, setLoading] = useState(false)
  const [currentPage, setCurrentPage] = useState(1)
  const [items, setItems] = useState<any[]>([])
  const [filterForm, setFilterForm] = useState({ id: '', name: '' })
  const [form, setForm] = useState({
    index: '',
    key: '',
    item: '',
    options: [] as string[]
  })

    
  const itemsPerPage = 6

  const totalPages = Math.ceil(items.length / itemsPerPage)
  const paginatedItems = items.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  )


  useEffect(() => {
    loadItems()
  }, [])

  async function loadItems() {
    try {
      setLoading(true)
      const response = await api.get('/items')
      setItems(response.data.sort((a, b) => a.index - b.index))
      setCurrentPage(1)
    } catch {
      toast.error('Erro ao carregar itens')
    } finally {
      setLoading(false)
    }
  }

  async function handleFilterSubmit(e: React.FormEvent) {
    e.preventDefault()
    try {
      setLoading(true)
      const params: Record<string, string> = {}
      if (filterForm.id) params.index = filterForm.id
      if (filterForm.name) params.item = filterForm.name

      const response = await api.get('/items/filters', { params });
      setItems(response.data.sort((a, b) => a.index - b.index))
      setCurrentPage(1)
    } catch {
      toast.error('Erro ao filtrar itens');
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(key: string) {
    const confirmed = window.confirm('Tem certeza que deseja excluir este item?')
    if (!confirmed) return

    setLoading(true)
    const toastId = toast.loading('Excluindo item...')

    try {
      await api.delete(`/items/${key}`)
      toast.dismiss(toastId)
      toast.success('Item excluído com sucesso!')
      loadItems()
    } catch {
      toast.dismiss(toastId)
      toast.error('Erro ao excluir item.')
    } finally {
      setLoading(false)
    }
  }


  function genQuestion(nome: string, opcoes: string[]) {
    return `${nome}: ${opcoes.length > 0 ? '?' : 'OK?'}`
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    const toastId = toast.loading('Criando item...');
    try {
      const payload = {
        index: Number(form.index),
        key: form.key.toLowerCase(),
        item: form.item.toUpperCase(),
        question: genQuestion(form.item, form.options),
        options: form.options
      }

      const response = await api.post('/items', payload);
      toast.dismiss(toastId);
      toast.success('Item criado com sucesso!');
      console.log(response.data);
      setForm({ index: '', key: '', item: '', options: [] });
      loadItems();
    } catch {
      toast.dismiss(toastId);
      toast.error('Erro ao salvar item.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <MainLayout>
    <div className='p-6 max-w-4xl mx-auto space-y-4'>
      <h1 className='text-3xl font-bold'>Itens</h1>
      <Card>
      <CardContent>
        <CardHeader>
      <div className='flex items-center justify-between'>
        <form className='flex items-center gap-2' onSubmit={handleFilterSubmit}>
          <Input
            name='id'
            placeholder='Índice do Item'
            className='w-auto'
            value={filterForm.id}
            onChange={(e) => setFilterForm({ ...filterForm, id: e.target.value })}
          />
          <Input
            name='name'
            placeholder='Nome do Item'
            className='w-auto'
            value={filterForm.name}
            onChange={(e) => setFilterForm({ ...filterForm, name: e.target.value })}
          />
          <Button type='submit' variant='link'>
            <Search className='w-4 h-4 mr-2' />
            Filtrar Resultados
          </Button>
        </form>
        
        <Dialog>
          <DialogTrigger asChild>
            <Button className='text-white bg-zinc-600'>
              <PlusCircle className='w-4 h-4 mr-2' />
              Novo Item
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Novo Item</DialogTitle>
              <DialogDescription>Adicionar um novo item no sistema</DialogDescription>
            </DialogHeader>
            <form onSubmit={handleSubmit} className='space-y-6'>
              <div className='grid grid-cols-4 items-center text-right gap-4'>
                <Label htmlFor='index'>Índice</Label>
                <Input
                  placeholder='Ex: 0'
                  className='col-span-3'
                  id='index'
                  value={form.index}
                  onChange={(e) => setForm({ ...form, index: e.target.value })}
                  required
                />
              </div>
              <div className='grid grid-cols-4 items-center text-right gap-4'>
                <Label htmlFor='key'>Chave do Item</Label>
                <Input
                  placeholder='Ex: item_modelo'
                  className='col-span-3'
                  id='key'
                  value={form.key}
                  onChange={(e) => setForm({ ...form, key: e.target.value })}
                  required
                />
              </div>
              <div className='grid grid-cols-4 items-center text-right gap-4'>
                <Label htmlFor='item'>Nome do Item</Label>
                <Input
                  placeholder='Ex: Modelo'
                  className='col-span-3'
                  id='item'
                  value={form.item}
                  onChange={(e) => setForm({ ...form, item: e.target.value })}
                  required
                />
              </div>
              <div className='grid grid-cols-4 items-center text-right gap-4'>
                <Label htmlFor='options'>Opções</Label>
                <div className='text-left col-span-3'>
                  <CreatableSelect
                    isMulti
                    placeholder='Digite e pressione Enter...'
                    onChange={(selected) => {
                      const values = selected.map((item) => item.value)
                      setForm({ ...form, options: values })
                    }}
                    value={form.options.map((opt) => ({ label: opt, value: opt }))}
                    formatCreateLabel={(inputValue) => `Criar "${inputValue}"`}
                  />
                </div>
              </div>
              <DialogFooter>
                <DialogClose asChild>
                  <Button type='button' variant='outline'>
                    Cancelar
                  </Button>
                </DialogClose>
                <Button
                  type='submit'
                  disabled={loading}
                  className={`text-white ${loading ? 'bg-gray-300' : 'bg-auto'}`}
                >
                  {loading ? 'Carregando...' : 'Salvar'}
                </Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>
      </div>
      </CardHeader>
        <Table>
          <TableHeader>
            <TableRow>
            <TableHead>Índice</TableHead>
            <TableHead>Item</TableHead>
            <TableHead>Nome</TableHead>
            <TableHead>Opções</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {items.length === 0 && !loading && (
              <TableRow>
                <TableCell colSpan={5} className='text-center'>
                  Nenhum item encontrado.
                </TableCell>
              </TableRow>
            )}

            {paginatedItems.map((item) => (
              <TableRow key={item.index}>
                <TableCell>{item.index}</TableCell>
                <TableCell>{item.key}</TableCell>
                <TableCell>{item.item}</TableCell>
                <TableCell className='w-auto'>{item.options?.join(', ')}</TableCell>
                <TableCell>
                    <Button
                    variant="ghost"
                    className='w-4 h-4 flex items-center justify-center p-0'
                    onClick={() => handleDelete(item.key)}>
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