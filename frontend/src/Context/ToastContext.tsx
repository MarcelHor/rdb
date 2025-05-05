import { createContext, useContext, useState } from "react";
import { Toast } from "react-bootstrap";
import type { ToastType } from "../utils/ToastEnum";

type ToastData = {
  type: ToastType;
  message: string;
};

const ToastContext = createContext({
  showToast: (_type: ToastType, _message: string) => {},
});

export const useToast = () => useContext(ToastContext);

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toast, setToast] = useState<ToastData | null>(null);

  const showToast = (type: ToastType, message: string) => {
    setToast({ type, message });
    setTimeout(() => setToast(null), 3000); // auto-hide
  };

  return (
    <ToastContext.Provider value={{ showToast }}>
      {children}
      {toast && (
        <div
          style={{
            position: "fixed",
            top: "1rem",
            left: "50%",
            transform: "translateX(-50%)",
            zIndex: 9999,
            minWidth: "250px",
          }}
        >
          <Toast bg={toast.type} show={true} onClose={() => setToast(null)}>
            <Toast.Header>
                <strong className="me-auto">
                  {
                    {
                      success: "Úspěch",
                      danger: "Chyba",
                      warning: "Varování",
                    }[toast.type]
                  }
                </strong>
            </Toast.Header>
            <Toast.Body className="text-white">{toast.message}</Toast.Body>
          </Toast>
        </div>
      )}
    </ToastContext.Provider>
  );
}
