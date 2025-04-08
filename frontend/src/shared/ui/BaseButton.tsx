import { defineComponent } from 'vue';

export default defineComponent({
  name: 'BaseButton',
  props: {
    type: {
      type: String,
      default: 'button'
    },
    variant: {
      type: String,
      default: 'primary'
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  setup(props, { slots }) {
    // Compute classes based on variant
    const getButtonClasses = () => {
      const baseClasses = 'py-2 px-4 rounded-md font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors';
      
      const variantClasses = {
        primary: 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500',
        secondary: 'bg-gray-200 text-gray-800 hover:bg-gray-300 focus:ring-gray-500',
        danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
        success: 'bg-green-600 text-white hover:bg-green-700 focus:ring-green-500',
        github: 'bg-gray-800 text-white hover:bg-gray-900 focus:ring-gray-700',
      };
      
      const disabledClasses = props.disabled ? 'opacity-50 cursor-not-allowed' : '';
      
      return `${baseClasses} ${variantClasses[props.variant as keyof typeof variantClasses] || variantClasses.primary} ${disabledClasses}`;
    };
    
    return () => (
      <button 
        type={props.type} 
        class={getButtonClasses()} 
        disabled={props.disabled}
      >
        {slots.default?.()}
      </button>
    );
  }
});
