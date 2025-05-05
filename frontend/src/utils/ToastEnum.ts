export const ToastType = {
    Success: "success",
    Error: "danger",
    Warning: "warning",
  } as const;
  
  export type ToastType = (typeof ToastType)[keyof typeof ToastType];
  