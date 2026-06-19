export default defineAppConfig({
  foo: 'bar',
  ui: {
    input: {
      variants: {
        size: {
          md: {
            base: 'text-base w-full',
          },
        },
      },
      defaultVariants: {
        color: 'info',
      },
      slots: {
        root: 'w-full',
        base: 'rounded-xl border-[#E5E7EB] bg-white text-[#1E293B] shadow-sm transition duration-200 placeholder:text-[#94A3B8] focus-visible:ring-2 focus-visible:ring-[#3B82F6]/25 focus-visible:border-[#3B82F6]',
      },
    },
    inputNumber: {
      defaultVariants: {
        color: 'info',
      },
      slots: {
        base: 'rounded-xl border-[#E5E7EB] bg-white text-[#1E293B] shadow-sm transition duration-200 focus-visible:ring-2 focus-visible:ring-[#3B82F6]/25 focus-visible:border-[#3B82F6]',
      },
    },
    button: {
      defaultVariants: {
        color: 'info',
      },
      slots: {
        base: 'rounded-xl font-semibold transition duration-200',
      },
    },
    textarea: {
      defaultVariants: {
        color: 'info',
      },
      slots: {
        root: 'w-full',
        base: 'rounded-xl border-[#E5E7EB] bg-white text-[#1E293B] shadow-sm transition duration-200 placeholder:text-[#94A3B8] focus-visible:ring-2 focus-visible:ring-[#3B82F6]/25 focus-visible:border-[#3B82F6]',
      },
    },
    select: {
      defaultVariants: {
        color: 'info',
      },
      slots: {
        base: 'rounded-xl border-[#E5E7EB] bg-white text-[#1E293B] shadow-sm transition duration-200 focus-visible:ring-2 focus-visible:ring-[#3B82F6]/25 focus-visible:border-[#3B82F6]',
      },
    },
    formField: {
      slots: {
        label: 'text-sm font-semibold text-[#334155]',
        error: 'mt-1 text-sm font-medium text-red-500',
        help: 'text-sm text-[#64748B]',
      },
    },
  },
})
