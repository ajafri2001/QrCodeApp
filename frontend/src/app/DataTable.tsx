'use client'

import { useEffect, useState } from 'react'
import { Trash2 } from 'lucide-react'
import { deleteQr } from '@/lib/api'

import type {
    ColumnDef,
    ColumnFiltersState,
    SortingState,
    VisibilityState
} from '@tanstack/react-table'

import {
    flexRender,
    getCoreRowModel,
    getFilteredRowModel,
    getPaginationRowModel,
    getSortedRowModel,
    useReactTable
} from '@tanstack/react-table'

import { Checkbox } from '@/components/ui/checkbox'
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'

export type History = {
    id: string
    originalUrl: string
    mimeType: 'PNG' | 'SVG'
    createdAt: string
}


const DataTable = ({ refreshKey, onRefresh }: {
    refreshKey: number
    onRefresh: () => void
}) => {
    const [sorting, setSorting] = useState<SortingState>([
        { id: 'createdAt', desc: true }
    ])
    const [columnFilters, setColumnFilters] =
        useState<ColumnFiltersState>([])
    const [columnVisibility, setColumnVisibility] =
        useState<VisibilityState>({})
    const [rowSelection, setRowSelection] = useState({})

    const [data, setData] = useState<History[]>([])
    const [loading, setLoading] = useState(true)

    const columns: ColumnDef<History>[] = [
        {
            id: 'select',
            header: ({ table }) => (
                <Checkbox
                    checked={
                        table.getIsAllPageRowsSelected() ||
                        (table.getIsSomePageRowsSelected() && 'indeterminate')
                    }
                    onCheckedChange={value =>
                        table.toggleAllPageRowsSelected(!!value)
                    }
                    aria-label='Select all'
                />
            ),
            cell: ({ row }) => (
                <Checkbox
                    checked={row.getIsSelected()}
                    onCheckedChange={value =>
                        row.toggleSelected(!!value)
                    }
                    aria-label='Select row'
                />
            ),
            enableSorting: false,
            enableHiding: false
        },
        {
            accessorKey: 'createdAt',
            header: 'Date',
            cell: ({ row }) => {
                const dateStr = row.getValue<string>('createdAt')
                const date = new Date(dateStr)
                return <div>{date.toLocaleString()}</div>
            }
        },
        {
            accessorKey: 'originalUrl',
            header: 'Original URL',
            cell: ({ row }) => {
                const url = row.getValue<string>('originalUrl')
                return (
                    <Button
                        variant="link"
                        className="px-0 h-auto"
                        asChild
                    >
                        <a href={url} target="_blank" rel="noopener noreferrer">
                            {url}
                        </a>
                    </Button>
                )
            }
        },
        {
            accessorKey: 'mimeType',
            header: 'Type',
            cell: ({ row }) => {
                const type = row.getValue<'PNG' | 'SVG'>('mimeType')

                return (
                    <span
                        className={`px-2 py-1 rounded text-xs font-medium ${type === 'SVG'
                            ? 'bg-purple-100 text-purple-700'
                            : 'bg-green-100 text-green-700'
                            }`}
                    >
                        {type}
                    </span>
                )
            }
        },
        {
            id: 'download',
            header: 'QR Code',
            cell: ({ row }) => {
                const id = row.original.id

                return (
                    <Button
                        variant="outline"
                        size="sm"
                        asChild
                    >
                        <a href={`/api/download/${id}`} download>
                            Download
                        </a>
                    </Button>
                )
            }
        },
        {
            id: 'actions',
            header: '',
            cell: ({ row }) => {
                const id = row.original.id

                return (
                    <Button
                        variant="destructive"
                        size="icon"
                        onClick={async () => {
                            await deleteQr(id)
                            onRefresh()
                        }}
                    >
                        <Trash2 className="h-4 w-4" />
                    </Button>
                )
            }
        }
    ]


    useEffect(() => {
        const fetchHistory = async () => {
            try {
                const res = await fetch('/api/getHistory', {
                    credentials: 'include'
                })

                if (!res.ok) {
                    setData([])
                    return
                }

                const json = await res.json()
                setData(json)
            } catch {
                setData([])
            } finally {
                setLoading(false)
            }
        }

        fetchHistory()
    }, [refreshKey])

    const table = useReactTable({
        data,
        columns,
        onSortingChange: setSorting,
        onColumnFiltersChange: setColumnFilters,
        getCoreRowModel: getCoreRowModel(),
        getPaginationRowModel: getPaginationRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
        onColumnVisibilityChange: setColumnVisibility,
        onRowSelectionChange: setRowSelection,
        state: {
            sorting,
            columnFilters,
            columnVisibility,
            rowSelection
        },
        meta: {
            onRefresh
        }
    })

    if (loading) return <div className="p-4">Loading...</div>

    return (
        <div className='w-full'>
            <div className='rounded-md border'>
                <Table>
                    <TableHeader>
                        {table.getHeaderGroups().map(headerGroup => (
                            <TableRow key={headerGroup.id}>
                                {headerGroup.headers.map(header => (
                                    <TableHead key={header.id}>
                                        {header.isPlaceholder
                                            ? null
                                            : flexRender(
                                                header.column.columnDef.header,
                                                header.getContext()
                                            )}
                                    </TableHead>
                                ))}
                            </TableRow>
                        ))}
                    </TableHeader>

                    <TableBody>
                        {table.getRowModel().rows?.length ? (
                            table.getRowModel().rows.map(row => (
                                <TableRow
                                    key={row.id}
                                    data-state={
                                        row.getIsSelected() && 'selected'
                                    }
                                >
                                    {row.getVisibleCells().map(cell => (
                                        <TableCell key={cell.id}>
                                            {flexRender(
                                                cell.column.columnDef.cell,
                                                cell.getContext()
                                            )}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell
                                    colSpan={columns.length}
                                    className='h-24 text-center'
                                >
                                    No results.
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>
        </div>
    )
}

export default DataTable
