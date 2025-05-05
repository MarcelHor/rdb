import { Navbar, Container } from "react-bootstrap";

export default function Header() {
  return (
    <>
      <Navbar bg="dark" variant="dark">
        <Container fluid>
          <Navbar.Brand className="ms-3">RDB - WeatherApp</Navbar.Brand>
        </Container>
      </Navbar>
    </>
  );
}
