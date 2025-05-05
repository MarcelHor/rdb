import { Spinner } from "react-bootstrap";

export default function SpinnerComponent({ loading }: { loading: boolean }) {
  return (
    <>
      {loading && (
        <div
          style={{
            position: "fixed",
            zIndex: 9999,
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            background: "rgba(255,255,255,0.7)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          <Spinner animation="border" variant="primary" />
        </div>
      )}
    </>
  );
}
