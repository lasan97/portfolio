import { defineComponent } from 'vue';

export default defineComponent({
  name: 'BaseCard',
  props: {
    title: {
      type: String,
      default: ''
    }
  },
  setup(props, { slots }) {
    return () => (
      <div class="bg-white rounded-lg shadow-md overflow-hidden p-6">
        {props.title && (
          <h3 class="text-lg font-medium text-gray-900 mb-4">{props.title}</h3>
        )}
        <div>
          {slots.default?.()}
        </div>
      </div>
    );
  }
});
