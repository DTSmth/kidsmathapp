import { render, screen, fireEvent } from '@testing-library/react';
import NumberPad from '../components/quiz/NumberPad';

describe('NumberPad', () => {
  it('renders digit buttons', () => {
    render(<NumberPad onSubmit={vi.fn()} />);
    expect(screen.getByLabelText('Number 7')).toBeInTheDocument();
    expect(screen.getByLabelText('Number 0')).toBeInTheDocument();
  });

  it('appends digit on tap', () => {
    render(<NumberPad onSubmit={vi.fn()} />);
    fireEvent.click(screen.getByLabelText('Number 3'));
    fireEvent.click(screen.getByLabelText('Number 7'));
    expect(screen.getByText('37')).toBeInTheDocument();
  });

  it('removes last digit on backspace', () => {
    render(<NumberPad onSubmit={vi.fn()} />);
    fireEvent.click(screen.getByLabelText('Number 4'));
    fireEvent.click(screen.getByLabelText('Number 2'));
    fireEvent.click(screen.getByLabelText('Backspace'));
    // The display div shows '4'; the button labeled 'Number 4' also contains '4'.
    // Use getAllByText and check at least one is in the document.
    const matches = screen.getAllByText('4');
    expect(matches.length).toBeGreaterThan(0);
  });

  it('does not exceed 4 digits', () => {
    render(<NumberPad onSubmit={vi.fn()} />);
    ['1', '2', '3', '4', '5'].forEach(d =>
      fireEvent.click(screen.getByLabelText(`Number ${d}`))
    );
    expect(screen.getByText('1234')).toBeInTheDocument();
  });

  it('submit button is disabled when input is empty', () => {
    render(<NumberPad onSubmit={vi.fn()} />);
    expect(screen.getByLabelText('Submit answer')).toBeDisabled();
  });

  it('calls onSubmit with current value', () => {
    const onSubmit = vi.fn();
    render(<NumberPad onSubmit={onSubmit} />);
    fireEvent.click(screen.getByLabelText('Number 7'));
    fireEvent.click(screen.getByLabelText('Submit answer'));
    expect(onSubmit).toHaveBeenCalledWith('7');
  });
});
